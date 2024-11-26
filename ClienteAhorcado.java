import java.io.*;
import java.net.*;

public class ClienteAhorcado {
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

                // Manejo de la pregunta inicial sobre el modo de juego
                if (mensaje.contains("¿Quieres jugar contra la máquina (1) o contra otro jugador (2)?")) {
                    String opcion = teclado.readLine();
                    salida.println(opcion); // Enviar la elección al servidor
                    if ("2".equals(opcion)) {
                        System.out.println("Esperando al segundo jugador...");
                    }
                }

                // Jugador 1 ingresa la longitud de la palabra
                else if (mensaje.contains("Jugador 1, ingresa la longitud de la palabra:")) {
                    System.out.print("Ingresa la longitud de la palabra: ");
                    String longitud = teclado.readLine();
                    salida.println(longitud); // Enviar longitud al servidor
                }

                // Jugador 1 ingresa la palabra
                else if (mensaje.contains("Jugador 1, ahora ingresa la palabra.")) {
                    System.out.print("Ingresa la palabra: ");
                    String palabra = teclado.readLine();
                    salida.println(palabra); // Enviar la palabra al servidor
                }

                // Jugador 1 corrige la letra de Jugador 2
                else if (mensaje.contains("Corrige: 1 si está bien, 0 si está mal.")) {
                    System.out.print("¿Está la letra bien? (1 para sí, 0 para no): ");
                    String respuesta = teclado.readLine();
                    salida.println(respuesta); // Enviar la corrección

                    // Si está bien, preguntar por la posición
                    if ("1".equals(respuesta)) {
                        System.out.print("Ingresa la posición de la letra: ");
                        String posicion = teclado.readLine();
                        salida.println(posicion); // Enviar la posición
                    }
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
