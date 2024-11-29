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
    private static final String  fichero = "estadisticas.csv";


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

                    // Lógica para crear la partida contra la máquina
                    Partida partida = new Partida(cliente1, null, cargarPalabras());
                    synchronized (partidasTotales) {
                        partidasTotales++; // Incrementar el contador cuando se crea una partida
                        System.out.println("Numero de partidas: " + partidasTotales);
                    }
                    pool.execute(partida);

                } else if ("2".equals(opcion)) {
                    System.out.println("Esperando a Jugador 2...");
                    Socket cliente2 = serverSocket.accept();
                    System.out.println("Jugador 2 conectado.");

                    BufferedReader entrada2 = new BufferedReader(new InputStreamReader(cliente2.getInputStream()));
                    PrintWriter salida2 = new PrintWriter(cliente2.getOutputStream(), true);

                    Future<String> opcionElegida2 = pool.submit(() -> preguntarModo(salida2,entrada2));
                    String opcion2 = opcionElegida2.get();

                    if ("2".equals(opcion2)) {
                        System.out.println("El Jugador 2 se ha unido a la partida.");
                        // Lógica de crear la partida entre 2 jugadores
                        Partida partida = new Partida(cliente1, cliente2, cargarPalabras());
                        pool.execute(partida);
                        synchronized (partidasTotales) {
                            partidasTotales++;
                            System.out.println("Numero de partidas: " + partidasTotales);
                        }
                    } else {
                        salida2.println("Opción inválida. Desconectando.");
                        cliente2.close();
                    }

                } else if("3".equals(opcion)) {
                    mostrarRanking(salida1);
                    cliente1.close();
                }
                else if("4".equals(opcion)) {
                    salida1.println("Introduce tu nickname: ");
                    String nombre = entrada1.readLine();
                    buscarPuntuacion(nombre,salida1);
                    cliente1.close();
                }

                else {
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
        salida.println("¿Quieres jugar contra la máquina (1), contra otro jugador (2), ver el ranquing (3), ver tu puntuacion (4)?");
        String opcion = entrada.readLine();
        System.out.println("Jugador 1 eligió: " + opcion);
        return opcion;
    }


    private static List<Jugador> cargarEstadisticas() {
        List<Jugador> jugadores = new ArrayList<>();
        File archivo = new File(fichero);

        if (archivo.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] datos = linea.split(",");
                    if (datos.length == 3) {
                        try {
                            String nombre = datos[0].trim();
                            int partidasGanadas = Integer.parseInt(datos[1].trim());
                            int partidasPerdidas = Integer.parseInt(datos[2].trim());

                            Jugador jugador = new Jugador(nombre);
                            jugador.setPartidasGanadas(partidasGanadas);
                            jugador.setPartidasPerdidas(partidasPerdidas);
                            jugadores.add(jugador);
                        } catch (NumberFormatException e) {
                            System.out.println("Fichero leido.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jugadores;
    }



    private static void mostrarRanking(PrintWriter salida) {
        List<Jugador> jugadores = cargarEstadisticas();  // Cargar las estadísticas del archivo
        if (jugadores != null && !jugadores.isEmpty()) {
            // Ordenar de mayor las partidas ganadas
            jugadores.sort(Comparator.comparingInt(Jugador::getPartidasGanadas).reversed());

            System.out.println("Ranking de los mejores jugadores:");
            for (int i = 0; i < Math.min(5, jugadores.size()); i++) {
                Jugador j = jugadores.get(i);
                salida.println((i + 1) + ". " + j.getNombre() + " - " + j.getPartidasGanadas() + " partidas ganadas");
                System.out.println((i + 1) + ". " + j.getNombre() + " - " + j.getPartidasGanadas() + " partidas ganadas");
            }
        } else {
            System.out.println("No hay estadísticas disponibles.");
        }
    }

    private static void buscarPuntuacion(String nombre, PrintWriter salida) {
        List<Jugador> jugadores = cargarEstadisticas();

        // Buscamos si el jugador existe en la lista
        boolean encontrado = false;
        for (Jugador jugador : jugadores) {
            if (jugador.getNombre().equalsIgnoreCase(nombre)) {
                // Si lo encontramos, mostramos sus puntuaciones
                salida.println(jugador.getPuntuacion());
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            salida.println("Jugador no encontrado.");
        }
    }

}
