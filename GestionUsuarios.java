import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.*;


public class GestionUsuarios implements Runnable{
    private Socket usuario;
    private static BlockingQueue<Socket> colaJugadores = new LinkedBlockingQueue<>();
    private static Integer partidasTotales = 0;
    private static final String  fichero = "estadisticas.csv";
    private static ExecutorService pool = Executors.newCachedThreadPool();

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

                if ("1".equals(opcion)) {
                    // Lógica para jugar contra la máquina
                    Partida partida = new Partida(usuario, null, cargarPalabras());
                    incrementarPartidas();
                    pool.execute(partida);

                } else if ("2".equals(opcion)) {
                    synchronized (colaJugadores) {
                        colaJugadores.add(usuario);
                        if (colaJugadores.size() >= 2) {
                            // Emparejar jugadores
                            // Espera al primer jugador
                            Socket jugador1 = colaJugadores.take();

                            // Espera al segundo jugador
                            Socket jugador2 = colaJugadores.take();

                            Partida partida = new Partida(jugador1, jugador2, cargarPalabras());
                            incrementarPartidas();
                            pool.execute(partida);
                        } else {
                            salida.println("Esperando a otro jugador...");
                        }
                    }
                } else if ("3".equals(opcion)) {
                    mostrarRanking(salida);
                    boolean continuar = preguntar(salida,entrada);
                    if (continuar) {
                        pool.execute(new GestionUsuarios(usuario));
                    }else{
                        usuario.close();
                    }

                } else if ("4".equals(opcion)) {
                    salida.println("Introduce tu nickname: ");
                    String nombre = entrada.readLine();
                    buscarPuntuacion(nombre, salida);
                    boolean continuar = preguntar(salida,entrada);
                    if (continuar) {
                        pool.execute(new GestionUsuarios(usuario));
                    }else{
                        usuario.close();
                    }

                } else {
                    salida.println("Opción inválida. Cerrando conexión.");
                    usuario.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    private boolean preguntar(PrintWriter salida, BufferedReader entrada) throws IOException {
        salida.println("¿Quieres seguir? (s/n)");
        String respuesta = entrada.readLine().trim().toLowerCase();
        return "s".equals(respuesta);
    }

        private static void incrementarPartidas() {
            synchronized (partidasTotales) {
                partidasTotales++;
                System.out.println("Número de partidas: " + partidasTotales);
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
