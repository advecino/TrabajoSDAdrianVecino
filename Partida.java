import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Partida implements Runnable {
    private final Socket jugador1;
    private final Socket jugador2; // Null si es un jugador contra la máquina
    private final List<String> palabras;
    private boolean partidaTerminada;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
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



            while (continuar) {

                if (jugador2==null) {// Modo contra la máquina
                    salida1.println("Introduce tu nickname: ");
                    String nombre = entrada1.readLine();
                    Jugador jugador = new Jugador(nombre);
                    while(continuar){

                        jugarContraMaquina(salida1, entrada1,jugador);
                        continuar = preguntarSiQuiereJugarOtraPartidaSolo(salida1, entrada1);
                    }

                        salida1.println(jugador.getPuntuacion());
                        if (crearFichero()) {
                            actualizarFichero(nombre,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                        }else{
                            crearFichero();
                            actualizarFichero(nombre,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                        }



                } else { // Modo 2 jugadores
                    try (BufferedReader entrada2 = new BufferedReader(new InputStreamReader(jugador2.getInputStream()));
                         PrintWriter salida2 = new PrintWriter(jugador2.getOutputStream(), true))
                    {

                        Future<String> pedirNombre1 = executor.submit(() -> pedirNombre(salida1, entrada1));

                        Future<String> pedirNombre2 = executor.submit(() -> pedirNombre(salida2, entrada2));

                        String nombre1 = pedirNombre1.get();
                        Jugador jugador = new Jugador(nombre1);
                        String nombre2 = pedirNombre2.get();

                        boolean nombreBien = true;
                        while (nombreBien) {
                            if(nombre1.equals(nombre2)){
                                nombreBien = true;
                                salida2.println("No puedes tener el mismo nombre que tu rival, ponte otro:");
                                nombre2 = pedirNombre(salida2, entrada2);
                            }else{
                                nombreBien = false;
                            }
                        }

                        // Crear el jugador con el nombre válido
                        Jugador jugador02 = new Jugador(nombre2);

                        while(continuar){
                            jugarContraJugador(salida1, entrada1, salida2, entrada2, jugador,jugador02);

                            Future<Boolean> respuestaJugador1 = executor.submit(() -> preguntarSiQuiereJugarOtraPartida(salida1, entrada1));
                            Future<Boolean> respuestaJugador2 = executor.submit(() -> preguntarSiQuiereJugarOtraPartida(salida2, entrada2));


                            boolean respuesta1 = respuestaJugador1.get();
                            boolean respuesta2 = respuestaJugador2.get();

                            continuar = respuesta1 && respuesta2;

                            if(!continuar){
                                if(respuesta1){
                                    salida1.println("Tu rival no quiere jugar mas partidas.");
                                }
                                salida1.println(jugador.getPuntuacion());
                                cerrarSocket(jugador1);
                                if(respuesta2){
                                    salida2.println("Tu rival no quiere jugar mas partidas.");
                                }
                                salida2.println(jugador02.getPuntuacion());
                                cerrarSocket(jugador2);
                                if (crearFichero()) {
                                    actualizarFichero(nombre1,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                                    actualizarFichero(nombre2,jugador02.getPartidasGanadas(),jugador02.getPartidasPerdidas());
                                }else{
                                    crearFichero();
                                    actualizarFichero(nombre1,jugador.getPartidasGanadas(),jugador.getPartidasPerdidas());
                                    actualizarFichero(nombre2,jugador02.getPartidasGanadas(),jugador02.getPartidasPerdidas());
                                }
                            }
                        }
                    } catch (ExecutionException | InterruptedException e) {
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

    private String pedirNombre(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("Introduce tu nickname: ");
        String nombre = entrada.readLine();
        salida.println("Esperando  a tu rival...");
        return nombre;
    }

    private void jugarContraMaquina(PrintWriter salida, BufferedReader entrada, Jugador jugador) throws IOException {
        System.out.println("Cargando palabra para la partida...");
        String palabra = palabras.get(new Random().nextInt(palabras.size()));
        System.out.println("Palabra seleccionada: " + palabra);

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
            System.out.println("Letra recibida: " + letra);

            if (letra.length() != 1) {
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

            if (letra.length() != 1) {
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

        if (!archivo.exists()) {
            try {
                if (archivo.createNewFile()) {
                    System.out.println("El archivo fue creado exitosamente.");
                    // Escribir la cabecera si es necesario
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichero))) {
                        writer.write("nombre,partidasGanadas,partidasPerdidas");  // Cabecera
                        writer.newLine();  // Nueva línea después del encabezado
                    }
                    return true;  // Archivo creado y cabecera escrita
                } else {
                    System.out.println("No se pudo crear el archivo.");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;  // El archivo ya existe
        }
    }

    private void actualizarFichero(String nombre, int partidasGanadas, int partidasPerdidas) {
        File archivo = new File(fichero);
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;

        // Intentar leer el archivo y buscar si el jugador ya existe
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                String nombreExistente = datos[0];

                if (nombreExistente.equals(nombre)) {
                    int partidasGanadasExistentes = Integer.parseInt(datos[1]);
                    int partidasPerdidasExistentes = Integer.parseInt(datos[2]);

                    partidasGanadas += partidasGanadasExistentes;
                    partidasPerdidas += partidasPerdidasExistentes;

                    encontrado = true;
                }

                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Hubo un problema al leer el archivo.");
        }

        // Si el jugador no existe, se añade como una nueva línea
        if (!encontrado) {
            lineas.add(nombre + "," + partidasGanadas + "," + partidasPerdidas);
        } else {
            // Si el jugador ya existe, se actualiza la línea con las nuevas estadísticas
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
                writer.newLine();
            }
            System.out.println("Archivo actualizado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Hubo un problema al escribir en el archivo.");
        }
    }



}