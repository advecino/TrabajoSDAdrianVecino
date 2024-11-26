import java.io.*;
import java.net.*;

public class ClienteAhorcado2 {//Cliente igual que el 1
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor de Ahorcado.");
            System.out.println("Esperando a que el jugador 1 elija la palabra");

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                System.out.println(mensaje);

                // Manejo de la pregunta inicial sobre el modo de juego
                if (mensaje.contains("¿Quieres jugar contra la máquina (1) o contra otro jugador (2)?")) {
                    System.out.print("Elige un modo de juego (1 o 2): ");
                    String opcion = teclado.readLine();
                    salida.println(opcion); // Enviar la elección al servidor
                }

                // Manejo del turno del jugador
                else if (mensaje.contains("Ingresa una letra:")) {
                    System.out.print("Tu letra: ");
                    String letra = teclado.readLine();
                    salida.println(letra); // Enviar la letra al servidor
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
