import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ServidorAhorcado {
    private static final int PUERTO = 12345;


        public static void main(String[] args) {
            ExecutorService pool = Executors.newCachedThreadPool(); // Para manejar hilos de partidas

            try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
                System.out.println("Servidor iniciado en el puerto " + PUERTO);

                while (true) {
                    Socket cliente1 = serverSocket.accept();
                    System.out.println("Jugador 1 conectado.");
                    BufferedReader entrada1 = new BufferedReader(new InputStreamReader(cliente1.getInputStream()));
                    PrintWriter salida1 = new PrintWriter(cliente1.getOutputStream(), true);

                    // Preguntar al jugador 1 por el modo de juego
                    salida1.println("¿Quieres jugar contra la máquina (1) o contra otro jugador (2)?");
                    String opcion = entrada1.readLine();
                    System.out.println("Jugador 1 eligió: " + opcion); // Depuración

                    if ("1".equals(opcion)) {
                        System.out.println("Creando partida contra la máquina.");
                        pool.execute(new Partida(cliente1, null, cargarPalabras()));
                    } else if ("2".equals(opcion)) {
                        System.out.println("Esperando a Jugador 2...");
                        Socket cliente2 = serverSocket.accept();
                        System.out.println("Jugador 2 conectado.");
                        pool.execute(new Partida(cliente1, cliente2, cargarPalabras()));
                    } else {
                        System.out.println("Opción inválida. Cerrando conexión.");
                        salida1.println("Opción inválida. Desconexión.");
                        cliente1.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // Método para cargar las palabras desde un archivo
    private static List<String> cargarPalabras() {
        try {
            return Files.readAllLines(Paths.get("palabras.txt"));
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de palabras.");
            e.printStackTrace();
            return Arrays.asList("ejemplo", "palabra", "java"); // Palabras por defecto en caso de error
        }
    }
}
