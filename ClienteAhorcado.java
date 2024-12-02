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

            System.out.println("Conectado al servidor de Ahorcado");

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (mensaje.trim().isEmpty()) {
                    continue; // Ignorar mensajes vacíos
                }

                System.out.println(mensaje);

                // Elegir modo
                if (mensaje.contains("¿Quieres jugar contra la máquina (1), contra otro jugador (2), ver el ranquing (3), ver tu puntuacion (4)?")) {
                    String opcion = teclado.readLine();
                    salida.println(opcion); // Enviar la elección
                    if ("3".equals(opcion)) {
                        System.out.println("Ranking:");
                    }
                }

                else if (mensaje.contains("Jugador 1, ingresa la palabra:")) {
                    String palabra = teclado.readLine();
                    salida.println(palabra); // Enviar longitud al servidor
                }

                else if (mensaje.contains("Ingresa una letra:")) {
                    String letra = teclado.readLine();
                    salida.println(letra); // Enviar la letra al servidor
                }

                else if (mensaje.contains("Introduce tu nickname: ")) {
                    String nombre = teclado.readLine();
                    salida.println(nombre);
                }

                else if (mensaje.contains("¿Quieres jugar otra partida? (s/n)") || mensaje.contains("¿Quieres seguir? (s/n)")) {
                    String respuesta = teclado.readLine();
                    salida.println(respuesta);
                    if ("n".equalsIgnoreCase(respuesta)) {
                        System.out.println("Saliendo del juego. Gracias por jugar.");
                        break;
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}