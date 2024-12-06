import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.*;
import java.util.logging.Logger;


public class GestionUsuarios implements Runnable{
    private final Socket usuario;
    private static final BlockingQueue<Socket> colaJugadores = new LinkedBlockingQueue<>();
    private static final Object lock = new Object(); // Objeto dedicado para sincronización
    private static volatile int partidasTotales = 0;
    private static final String  fichero = "estadisticas.csv";
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Logger LOGGER = Logger.getLogger(GestionUsuarios.class.getName());


    public GestionUsuarios(Socket socket){
        this.usuario = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(usuario.getInputStream()));
            PrintWriter salida = new PrintWriter(usuario.getOutputStream(), true);

            salida.println("¿Quieres jugar contra la máquina (1), contra otro jugador (2), ver el ranquing (3), ver tu puntuacion (4)?");
            String opcion = entrada.readLine();

            switch (opcion) {
                case "1" -> {
                    // Lógica para jugar contra la máquina
                    Partida partida = new Partida(usuario, null, cargarPalabras());
                    incrementarPartidas();
                    pool.execute(partida);
                }
                case "2" -> {
                    synchronized (colaJugadores) {
                        colaJugadores.add(usuario);
                        if (colaJugadores.size() >= 2) {
                            Socket jugador1 = colaJugadores.take();

                            Socket jugador2 = colaJugadores.take();

                            Partida partida = new Partida(jugador1, jugador2, cargarPalabras());
                            incrementarPartidas();
                            pool.execute(partida);
                        } else {
                            salida.println("Esperando a otro jugador...");
                        }
                    }
                }
                case "3" -> {
                    mostrarRanking(salida);
                    boolean continuar = preguntar(salida, entrada);
                    if (continuar) {
                        pool.execute(new GestionUsuarios(usuario));
                    } else {
                        usuario.close();
                    }

                }
                case "4" -> {
                    salida.println("Introduce tu nickname: ");
                    String nombre = entrada.readLine();
                    buscarPuntuacion(nombre, salida);
                    boolean continuar = preguntar(salida, entrada);
                    if (continuar) {
                        pool.execute(new GestionUsuarios(usuario));
                    } else {
                        usuario.close();
                    }

                }
                case null, default -> {
                    salida.println("Opción inválida. Cerrando conexión.");
                    usuario.close();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Se produjo una excepción: " + e.getMessage());
            LOGGER.throwing(GestionUsuarios.class.getName(), "run", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean preguntar(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("¿Quieres seguir? (s/n)");
        String respuesta = entrada.readLine().trim().toLowerCase();
        return "s".equals(respuesta);
    }

    public static void incrementarPartidas() {
        synchronized (lock) {
            partidasTotales++;
            System.out.println("Número de partidas: " + partidasTotales);
        }
    }



    private static List<String> cargarPalabras() {
        try {
            return Files.readAllLines(Paths.get("palabras.txt"));
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de palabras.");
            LOGGER.severe("Se produjo una excepción: " + e.getMessage());
            LOGGER.throwing(ClienteAhorcado.class.getName(), "cargarPalabras", e);
            return Arrays.asList("ejemplo", "palabra", "java");
        }
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
                LOGGER.severe("Se produjo una excepción: " + e.getMessage());
                LOGGER.throwing(ClienteAhorcado.class.getName(), "cargarEstadisticas", e);
            }
        }
        return jugadores;
    }



    private static void mostrarRanking(PrintWriter salida) {
        List<Jugador> jugadores = cargarEstadisticas();  // Cargar las estadísticas del archivo
        if (!jugadores.isEmpty()) {
            // Ordenar de mayor las partidas ganadas
            jugadores.sort(Comparator.comparingInt(Jugador::getPartidasGanadas).reversed());

            System.out.println("Mostrando ranking de los mejores jugadores.");
            for (int i = 0; i < Math.min(5, jugadores.size()); i++) {
                Jugador j = jugadores.get(i);
                salida.println((i + 1) + ". " + j.getNombre() + " - " + j.getPartidasGanadas() + " partidas ganadas");
            }
        } else {
            System.out.println("No hay estadísticas disponibles.");
        }
    }

    private static void buscarPuntuacion(String nombre, PrintWriter salida) {
        List<Jugador> jugadores = cargarEstadisticas();

        boolean encontrado = false;
        for (Jugador jugador : jugadores) {
            if (jugador.getNombre().equalsIgnoreCase(nombre)) {
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