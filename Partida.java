import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Partida implements Runnable {
    private Socket jugador1;
    private Socket jugador2; // Null si es un jugador contra la máquina
    private List<String> palabras;
    private boolean partidaTerminada;
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    static List<Jugador> jugadores = new ArrayList<>();
    private final String  fichero = "estadisticas.csv";

    public Partida(Socket jugador1, Socket jugador2, List<String> palabras) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.palabras = palabras;
    }

    @Override
    public void run() {

        try (
                BufferedReader entrada1 = new BufferedReader(new InputStreamReader(jugador1.getInputStream()));
                PrintWriter salida1 = new PrintWriter(jugador1.getOutputStream(), true)
        ) {
            boolean continuar = true;
            salida1.println("Introduce tu nickname: ");
            String nombre = entrada1.readLine();
            Jugador jugador = new Jugador(nombre);
            jugadores.add(jugador);

            while (continuar) {

                if (jugador2==null) {

                    // Modo de un jugador contra la máquina
                    jugarContraMaquina(salida1, entrada1,jugador);
                    continuar = preguntarSiQuiereJugarOtraPartidaSolo(salida1, entrada1);
                    if(!continuar) {
                        salida1.println(jugador.getPuntuacion());
                        if (crearFichero()) {
                            actualizarFichero(nombre,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                        }else{
                            crearFichero();
                            actualizarFichero(nombre,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                        }
                    }


                } else {
                    try (
                            BufferedReader entrada2 = new BufferedReader(new InputStreamReader(jugador2.getInputStream()));
                            PrintWriter salida2 = new PrintWriter(jugador2.getOutputStream(), true)
                    ) {
                        salida1.println("Esperando al segundo jugador...");
                        boolean correcto = true;
                        String nombre2 = "";
                        while (correcto) {
                            salida2.println("Introduce tu nickname: ");
                            nombre2 = entrada2.readLine();
                            correcto = false;

                            for (Jugador j : jugadores) {
                                if (nombre2.equals(j.getNombre())) {
                                    correcto = true; // El nombre ya está en uso
                                    salida2.println("No te puedes poner el mismo nombre que tu rival.");
                                    break;
                                }
                            }
                        }

                        // Crear el jugador con el nombre válido
                        Jugador jugador02 = new Jugador(nombre2);
                        jugadores.add(jugador02);


                        while(continuar){
                            jugarContraJugador(salida1, entrada1, salida2, entrada2, jugador,jugador02);

                            Future<Boolean> respuestaJugador1 = executor.submit(() -> preguntarSiQuiereJugarOtraPartida(salida1, entrada1));
                            Future<Boolean> respuestaJugador2 = null;
                            respuestaJugador2 = executor.submit(() -> preguntarSiQuiereJugarOtraPartida(salida2, entrada2));


                            boolean respuesta1 = respuestaJugador1.get();
                            boolean respuesta2 = respuestaJugador2.get();

                            continuar = respuesta1 && respuesta2;

                            if(!continuar){
                                salida1.println(jugador.getPuntuacion());
                                cerrarSocket(jugador1);
                                salida2.println(jugador02.getPuntuacion());
                                cerrarSocket(jugador2);
                                if (crearFichero()) {
                                    actualizarFichero(nombre,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                                    actualizarFichero(nombre2,jugador02.getPartidasGanadas(),jugador02.getPartidasPerdidas());
                                }else{
                                    crearFichero();
                                    actualizarFichero(nombre,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                                    actualizarFichero(nombre2,jugador02.getPartidasGanadas(),jugador02.getPartidasPerdidas());
                                }
                            }
                        }
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(!jugador1.isClosed()){
                cerrarSocket(jugador1);
            }
            if (jugador2 != null && !jugador2.isClosed()) cerrarSocket(jugador2);
        }
    }

    private void jugarContraMaquina(PrintWriter salida, BufferedReader entrada, Jugador jugador) throws IOException {
        System.out.println("Cargando palabra para la partida..."); // Depuración
        String palabra = palabras.get(new Random().nextInt(palabras.size()));
        System.out.println("Palabra seleccionada: " + palabra); // Depuración

        char[] tablero = new char[palabra.length()];
        Arrays.fill(tablero, '_');
        int intentos = 8;



        salida.println("¡Comienza el juego contra la máquina!");
        while (intentos > 0) {
            salida.println("Palabra: " + String.valueOf(tablero));
            salida.println("Intentos restantes: " + intentos);
            dibujarAhorcado(intentos, salida);

            salida.println("Ingresa una letra:");
            String letra = entrada.readLine().trim().toLowerCase();
            System.out.println("Letra recibida: " + letra); // Depuración

            if (letra.isEmpty() || letra.length() > 1) {
                salida.println("Entrada inválida. Ingresa solo una letra.");
                continue;
            }

            boolean acierto = false;
            for (int i = 0; i < palabra.length(); i++) {
                if (palabra.charAt(i) == letra.charAt(0)) {
                    tablero[i] = letra.charAt(0);
                    acierto = true;
                }
            }

            if (!acierto) intentos--;

            if (String.valueOf(tablero).equals(palabra)) {
                salida.println("¡Felicidades, has ganado! La palabra era: " + palabra);
                setPartidaTerminada(true);
                jugador.ganarPartida();
                return;
            }

        }
        dibujarAhorcado(0, salida);
        jugador.perderPartida();
        salida.println("¡Has perdido! La palabra era: " + palabra);

    }

    private void jugarContraJugador(PrintWriter salida1, BufferedReader entrada1, PrintWriter salida2, BufferedReader entrada2,Jugador jugador1, Jugador jugador2) throws IOException {
        // Jugador 1 elige la palabra
        salida2.println("Tu rival esta eligiendo la palabra...");
        salida1.println("Jugador 1, ingresa la palabra:");
        String palabra = entrada1.readLine().trim().toLowerCase();
        char[] tablero = new char[palabra.length()];
        Arrays.fill(tablero, '_');
        int intentos = 8;

        salida2.println("¡Comienza el juego! La palabra tiene " + palabra.length() + " letras.");
        while (intentos > 0) {
            salida1.println("Palabra: " + String.valueOf(tablero));
            salida2.println("Palabra: " + String.valueOf(tablero));
            salida2.println("Intentos restantes: " + intentos);
            dibujarAhorcado(intentos, salida2);
            dibujarAhorcado(intentos, salida1);

            salida1.println("El rival esta eligiendo una letra...");
            salida2.println("Ingresa una letra:");
            String letra = entrada2.readLine().trim().toLowerCase();

            if (letra.isEmpty() || letra.length() > 1) {
                salida2.println("Entrada inválida. Ingresa solo una letra.");
                continue;
            }

            salida1.println("Letra recibida por el jugador 2: " + letra);
            boolean acierto = false;

            for (int i = 0; i < palabra.length(); i++) {
                if (palabra.charAt(i) == letra.charAt(0)) {
                    tablero[i] = letra.charAt(0);
                    acierto = true;
                }
            }

            if (!acierto) intentos--;

            if (String.valueOf(tablero).equals(palabra)) {
                salida2.println("¡Felicidades, has ganado! La palabra era: " + palabra);
                jugador2.ganarPartida();
                salida1.println("Tu rival ha ganado.");
                jugador1.perderPartida();
                setPartidaTerminada(true);
                return;
            }
        }
        dibujarAhorcado(0, salida2);
        salida2.println("¡Has perdido! La palabra era: " + palabra);
        jugador2.perderPartida();
        salida1.println("Tu rival ha perdido. ¡Tu ganas!");
        jugador1.ganarPartida();
    }

    private void dibujarAhorcado(int intentos, PrintWriter salida) {
        switch (intentos) {
            case 8: break;
            case 7 : intentos7(salida);break;
            case 6 : intentos6(salida);break;
            case 5 : intentos5(salida);break;
            case 4 : intentos4(salida);break;
            case 3 : intentos3(salida);break;
            case 2 : intentos2(salida);break;
            case 1 : intentos1(salida);break;
            default: intentos0(salida);break;
        }
    }

    private void cerrarSocket(Socket socket) {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void intentos7(PrintWriter salida){
        salida.println("|");
        salida.println("|");
        salida.println("|");
        salida.println("|");
        salida.println("|");
        salida.println("|");
    }

    private void intentos6(PrintWriter salida){
        salida.println("|------");
        salida.println("|");
        salida.println("|");
        salida.println("|");
        salida.println("|");
        salida.println("|");
    }

    private void intentos5(PrintWriter salida){
        salida.println("|------");
        salida.println("|   |");
        salida.println("|");
        salida.println("|");
        salida.println("|");
        salida.println("|");
    }

    private void intentos4(PrintWriter salida){
        salida.println("|------");
        salida.println("|   |");
        salida.println("| (^_^) ");
        salida.println("|");
        salida.println("|");
        salida.println("|");
    }

    private void intentos3(PrintWriter salida){
        salida.println("|------");
        salida.println("|   |");
        salida.println("| (^_^) ");
        salida.println("|   |");
        salida.println("|");
        salida.println("|");
    }

    private void intentos2(PrintWriter salida){
        salida.println("|------");
        salida.println("|   |");
        salida.println("| (^_^) ");
        salida.println("| º-|-º");
        salida.println("|");
        salida.println("|");
    }

    private void intentos1(PrintWriter salida){
        salida.println("|------");
        salida.println("|   |");
        salida.println("| (^_^) ");
        salida.println("| º-|-º");
        salida.println("|  /");
        salida.println("|");
        salida.println("¡¡¡ULTIMA OPORTUNIDAD!!!");
    }

    private void intentos0(PrintWriter salida){
        salida.println("|------");
        salida.println("|   |");
        salida.println("| (^_^) ");
        salida.println("| º-|-º");
        salida.println("|  / \\");
        salida.println("|");
    }



    public boolean isPartidaTerminada() {
        return partidaTerminada;
    }

    public void setPartidaTerminada(boolean partidaTerminada) {
        this.partidaTerminada = partidaTerminada;
    }


    private boolean preguntarSiQuiereJugarOtraPartida(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("¿Quieres jugar otra partida? (s/n)");
        String respuesta = entrada.readLine().trim().toLowerCase();
        if(respuesta.equals("s")){
            salida.println("Esperando la respuesta de tu rival");
        }
        return "s".equals(respuesta);
    }

    private boolean preguntarSiQuiereJugarOtraPartidaSolo(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("¿Quieres jugar otra partida? (s/n)");
        String respuesta = entrada.readLine().trim().toLowerCase();
        return "s".equals(respuesta);
    }


    private boolean crearFichero() {
        File archivo = new File(fichero);

        // Verificar si el archivo ya existe
        if (!archivo.exists()) {
            try {
                // Si no existe, lo creamos
                if (archivo.createNewFile()) {
                    System.out.println("El archivo fue creado exitosamente.");
                    // Escribir la cabecera (si es necesario)
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichero))) {
                        writer.write("nombre,partidasGanadas,partidasPerdidas");  // Cabecera
                        writer.newLine();  // Nueva línea después del encabezado
                    }
                    return true;  // Archivo creado y cabecera escrita
                } else {
                    System.out.println("No se pudo crear el archivo.");
                    return false;  // No se pudo crear el archivo
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;  // Error al intentar crear el archivo
            }
        } else {
            return true;  // El archivo ya existe, no es necesario crear uno nuevo
        }
    }

    private void actualizarFichero(String nombre, int partidasGanadas, int partidasPerdidas) {
        File archivo = new File(fichero);
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;

        // Intentar leer el archivo y buscar si el jugador ya existe
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;

            // Leer todas las líneas del archivo
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");  // Asumimos que los datos están separados por comas
                String nombreExistente = datos[0];  // Nombre del jugador

                if (nombreExistente.equals(nombre)) {
                    // Si encontramos al jugador, sumamos las partidas ganadas y perdidas
                    int partidasGanadasExistentes = Integer.parseInt(datos[1]);
                    int partidasPerdidasExistentes = Integer.parseInt(datos[2]);

                    partidasGanadas += partidasGanadasExistentes;  // Sumamos las partidas ganadas
                    partidasPerdidas += partidasPerdidasExistentes; // Sumamos las partidas perdidas

                    // Marcar que hemos encontrado al jugador
                    encontrado = true;
                }

                // Agregar la línea al listado para reescribir el archivo más tarde
                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Hubo un problema al leer el archivo.");
        }

        // Si el jugador no existe, lo añadimos como una nueva línea
        if (!encontrado) {
            lineas.add(nombre + "," + partidasGanadas + "," + partidasPerdidas);
        } else {
            // Si el jugador ya existe, actualizamos la línea con las nuevas estadísticas
            for (int i = 0; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(",");
                if (datos[0].equals(nombre)) {
                    lineas.set(i, nombre + "," + partidasGanadas + "," + partidasPerdidas);
                    break;
                }
            }
        }

        // Escribir el archivo de nuevo con las estadísticas actualizadas
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (String linea : lineas) {
                writer.write(linea);
                writer.newLine(); // Nueva línea después de cada registro
            }
            System.out.println("Archivo actualizado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Hubo un problema al escribir en el archivo.");
        }
    }


}