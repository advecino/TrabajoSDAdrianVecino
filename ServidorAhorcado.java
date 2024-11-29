import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.*;

public class ServidorAhorcado {
    private static final int PUERTO = 12345;
    private static ExecutorService pool = Executors.newCachedThreadPool(); // Para manejar hilos de partidas
    private static Integer partidasTotales = 0;


    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                // Aceptar conexiones de jugadores de forma continua
                Socket cliente1 = serverSocket.accept();
                System.out.println("Jugador 1 conectado.");
                BufferedReader entrada1 = new BufferedReader(new InputStreamReader(cliente1.getInputStream()));
                PrintWriter salida1 = new PrintWriter(cliente1.getOutputStream(), true);

                Future<String> opcionElegida1 = pool.submit(() -> preguntarModo(salida1,entrada1));
                String opcion = opcionElegida1.get();

                if ("1".equals(opcion)) {
                    System.out.println("Creando partida contra la máquina.");

                    // Aquí va la lógica para crear la partida contra la máquina
                    Partida partida = new Partida(cliente1, null, cargarPalabras());
                    synchronized (partidasTotales) {
                        partidasTotales++; // Incrementar el contador cuando se crea una partida
                        System.out.println("Numero de partidas: " + partidasTotales);
                    }
                    pool.execute(partida);

                } else if ("2".equals(opcion)) {
                    // Esperamos a Jugador 2
                    System.out.println("Esperando a Jugador 2...");
                    Socket cliente2 = serverSocket.accept();
                    System.out.println("Jugador 2 conectado.");

                    BufferedReader entrada2 = new BufferedReader(new InputStreamReader(cliente2.getInputStream()));
                    PrintWriter salida2 = new PrintWriter(cliente2.getOutputStream(), true);

                    Future<String> opcionElegida2 = pool.submit(() -> preguntarModo(salida2,entrada2));
                    String opcion2 = opcionElegida2.get();

                    if ("2".equals(opcion2)) {
                        System.out.println("El Jugador 2 se ha unido a la partida.");
                        // Lógica de crear la partida entre ambos jugadores
                        Partida partida = new Partida(cliente1, cliente2, cargarPalabras());
                        pool.execute(partida);
                        synchronized (partidasTotales) {
                            partidasTotales++; // Incrementar el contador cuando se crea una partida
                            System.out.println("Numero de partidas: " + partidasTotales);
                        }
                    } else {
                        salida2.println("Opción inválida. Desconectando.");
                        cliente2.close();
                    }
                } else {
                    salida1.println("Opción inválida. Cerrando conexión.");
                    cliente1.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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

    private static String preguntarModo(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("¿Quieres jugar contra la máquina (1) o contra otro jugador (2)?");
        String opcion = entrada.readLine();
        System.out.println("Jugador 1 eligió: " + opcion);
        return opcion;
    }
}
