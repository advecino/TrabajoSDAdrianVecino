import java.io.*;
import java.net.*;

public class ClienteAhorcado2 {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor de Ahorcado.");

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                System.out.println(mensaje);

                // Preguntar si quiere jugar contra la máquina o unirse a una partida
                if (mensaje.contains("¿Quieres jugar contra la máquina (1) o unirte a una partida existente (2)?")) {
                    String opcion = teclado.readLine();
                    salida.println(opcion);

                    if ("2".equals(opcion)) {
                        System.out.println("Esperando a que el servidor te asigne a una partida...");
                    }
                }


                // Cuando es el turno de Jugador 2
                else if (mensaje.contains("Ingresa una letra:")) {
                    System.out.print("Tu letra: ");
                    String letra = teclado.readLine();
                    salida.println(letra);
                }

                // Anuncio de fin de la partida
                else if (mensaje.contains("¡Has ganado!") || mensaje.contains("¡Has perdido!")) {
                    System.out.println(mensaje);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}