import java.io.*;
import java.net.*;
import java.util.*;

public class Partida implements Runnable {
    private Socket jugador1;
    private Socket jugador2; // Null si es un jugador contra la máquina
    private List<String> palabras;

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
            if (jugador2 == null) {
                // Modo de un jugador contra la máquina
                jugarContraMaquina(salida1, entrada1);
            } else {
                try (
                        BufferedReader entrada2 = new BufferedReader(new InputStreamReader(jugador2.getInputStream()));
                        PrintWriter salida2 = new PrintWriter(jugador2.getOutputStream(), true)
                ) {
                    // Modo de dos jugadores
                    jugarContraJugador(salida1, entrada1, salida2, entrada2);
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
            switch(intentos) {
                case 8: break;
                case 7: intentos7(salida); break;
                case 6: intentos6(salida); break;
                case 5: intentos5(salida); break;
                case 4: intentos4(salida); break;
                case 3: intentos3(salida); break;
                case 2: intentos2(salida); break;
                case 1: intentos1(salida); break;
            }

            salida.println("Ingresa una letra:");

            String letra = entrada.readLine();
            System.out.println("Letra recibida: " + letra); // Depuración
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
                return;
            }
        }
        intentos0(salida);
        salida.println("¡Has perdido! La palabra era: " + palabra);
    }

    private void jugarContraJugador(PrintWriter salida1, BufferedReader entrada1, PrintWriter salida2, BufferedReader entrada2) throws IOException {
        // Jugador 1 elige la palabra
        salida1.println("Jugador 1, ingresa la palabra:");
        String palabra = entrada1.readLine();
        int longitud = palabra.length();
        char[] tablero = new char[longitud];
        Arrays.fill(tablero, '_');
        int intentos = 8;

        salida2.println("¡Comienza el juego! La palabra tiene " + longitud + " letras.");
        while (intentos > 0) {
            salida2.println("Palabra: " + String.valueOf(tablero));
            salida2.println("Intentos restantes: " + intentos);
            salida1.println("Al jugador2 le quedan " + intentos + " intentos.");
            switch(intentos) {
                case 8: break;
                case 7: intentos7(salida2);intentos7(salida1); break;
                case 6: intentos6(salida2);intentos6(salida1); break;
                case 5: intentos5(salida2);intentos5(salida1); break;
                case 4: intentos4(salida2);intentos4(salida1); break;
                case 3: intentos3(salida2);intentos3(salida1); break;
                case 2: intentos2(salida2);intentos2(salida1); break;
                case 1: intentos1(salida2);intentos1(salida1); break;
            }


            salida2.println("Ingresa una letra:");

            String letra = entrada2.readLine();
            salida1.println("Letra recibida por el jugador2: " + letra);
            System.out.println("Letra recibida: " + letra); // Depuración
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
                return;
            }
        }
        intentos0(salida2);
        intentos0(salida1);
        salida2.println("¡Has perdido! La palabra era: " + palabra);
        salida1.println("¡Felicidades, has ganado!");
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



}