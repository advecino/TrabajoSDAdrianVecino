import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServidorAhorcado {
    private static final int PUERTO = 12345;
    private static ExecutorService pool = Executors.newCachedThreadPool(); // Para manejar hilos de partidas
    private static Integer partidasTotales = 0; // Contador de partidas activas

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                // Aceptar conexiones de jugadores de forma continua
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
                    // Incrementar el contador de partidas activas
                    synchronized (partidasTotales) {
                        partidasTotales++;
                        System.out.println("Numero de partidas: " + partidasTotales);
                    }
                    pool.execute(new Partida(cliente1, null, cargarPalabras()));
                } else if ("2".equals(opcion)) {
                    // Esperamos a Jugador 2 en paralelo sin bloquear el flujo
                    System.out.println("Esperando a Jugador 2...");
                    Socket cliente2 = serverSocket.accept();
                    System.out.println("Jugador 2 conectado.");

                    // Enviar al Cliente 2 la misma pregunta para elegir el modo de juego
                    PrintWriter salida2 = new PrintWriter(cliente2.getOutputStream(), true);
                    salida2.println("¿Quieres jugar contra la máquina (1) o unirte a una partida existente (2)?");

                    // Esperamos la respuesta del Cliente 2
                    BufferedReader entrada2 = new BufferedReader(new InputStreamReader(cliente2.getInputStream()));
                    String opcion2 = entrada2.readLine();
                    System.out.println("Jugador 2 eligió: " + opcion2);

                    if ("2".equals(opcion2)) {
                        System.out.println("El Jugador 2 se ha unido a la partida.");
                        salida2.println("Esperando a que el Jugador 1 elija la palabra...");
                        // Crear una nueva partida en paralelo
                        Partida partida = new Partida(cliente1, cliente2, cargarPalabras());
                        pool.execute(partida);
                        synchronized (partidasTotales) {
                            partidasTotales++; // Incrementar el contador cuando se crea una partida
                            System.out.println("Numero de partidas: " + partidasTotales);
                        }
                    } else {
                        salida2.println("El modo de juego no es válido. Desconectando.");
                        cliente2.close();
                    }
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

    // Cargar las palabras desde un archivo
    private static List<String> cargarPalabras() {
        try {
            // Intentar leer las palabras del archivo "palabras.txt"
            return Files.readAllLines(Paths.get("palabras.txt"));
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de palabras.");
            e.printStackTrace();
            // Palabras predeterminadas en caso de error
            return Arrays.asList("ejemplo", "palabra", "java");
        }
    }
}
