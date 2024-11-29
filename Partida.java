import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Partida implements Runnable {
    private Socket jugador1;
    private Socket jugador2; // Null si es un jugador contra la máquina
    private List<String> palabras;
    private boolean partidaTerminada;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

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

                if (jugador2==null) {
                    // Modo de un jugador contra la máquina
                    jugarContraMaquina(salida1, entrada1);
                    continuar = preguntarSiQuiereJugarOtraPartidaSolo(salida1, entrada1);
                } else {
                    try (
                            BufferedReader entrada2 = new BufferedReader(new InputStreamReader(jugador2.getInputStream()));
                            PrintWriter salida2 = new PrintWriter(jugador2.getOutputStream(), true)
                    ) {
                        while(continuar){
                            jugarContraJugador(salida1, entrada1, salida2, entrada2);

                            Future<Boolean> respuestaJugador1 = executorService.submit(() -> preguntarSiQuiereJugarOtraPartida(salida1, entrada1));
                            Future<Boolean> respuestaJugador2 = null;
                            respuestaJugador2 = executorService.submit(() -> preguntarSiQuiereJugarOtraPartida(salida2, entrada2));


                            boolean respuesta1 = respuestaJugador1.get(); // Espera la respuesta de jugador 1
                            boolean respuesta2 = respuestaJugador2.get();

                            continuar = respuesta1 && respuesta2;
                            if(!respuesta1){
                                salida2.println("Tu rival no quiere jugar otra partida");
                            }
                            if(!respuesta2){
                                salida1.println("Tu rival no quiere jugar otra partida");
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
            cerrarSocket(jugador1);
            if (jugador2 != null) cerrarSocket(jugador2);
        }
    }

    private void jugarContraMaquina(PrintWriter salida, BufferedReader entrada) throws IOException {
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
                return;
            }

        }
        dibujarAhorcado(0, salida);
        salida.println("¡Has perdido! La palabra era: " + palabra);
    }

    private void jugarContraJugador(PrintWriter salida1, BufferedReader entrada1, PrintWriter salida2, BufferedReader entrada2) throws IOException {
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
                salida1.println("Tu rival ha ganado.");
                setPartidaTerminada(true);
                return;
            }
        }
        dibujarAhorcado(0, salida2);
        salida2.println("¡Has perdido! La palabra era: " + palabra);
        salida1.println("Tu rival ha perdido. ¡Tu ganas!");
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
        salida.println("Esperando la respuesta de tu rival");
        return "s".equals(respuesta);
    }

    private boolean preguntarSiQuiereJugarOtraPartidaSolo(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("¿Quieres jugar otra partida? (s/n)");
        String respuesta = entrada.readLine().trim().toLowerCase();
        return "s".equals(respuesta);
    }


}