package src;

// Autor: Adrián Vecino Durán

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class ClienteAhorcado {
    private static final Logger LOGGER = Logger.getLogger(ClienteAhorcado.class.getName());
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


                System.out.println(mensaje);

                if (mensaje.contains("¿Quieres jugar contra la máquina (1), contra otro jugador (2), ver el ranquing (3), ver tu puntuacion (4)?")) {
                    String opcion = teclado.readLine();
                    salida.println(opcion); // Enviar la elección
                    if ("3".equals(opcion)) {
                        System.out.println("Ranking:");
                    }
                }

                else if (mensaje.contains("Jugador 1, ingresa la palabra:") || mensaje.contains("Ingresa una letra:") || mensaje.contains("Introduce tu nickname: "))  {
                    String palabra = teclado.readLine();
                    salida.println(palabra);
                }

                else if (mensaje.contains("¿Quieres jugar otra partida? (s/n)") || mensaje.contains("¿Quieres seguir? (s/n)")) {
                    String respuesta = teclado.readLine();
                    salida.println(respuesta);
                    if ("n".equalsIgnoreCase(respuesta)) {
                        System.out.println("Saliendo del juego. Gracias por jugar.");
                        if((mensaje = entrada.readLine()) != null){
                            System.out.println(mensaje);
                        }
                        break;
                    }

                }

            }

        } catch (Exception e) {
            LOGGER.severe("Se produjo una excepción: " + e.getMessage());
            LOGGER.throwing(ClienteAhorcado.class.getName(), "main", e);
        }
    }
}