import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ListasDeEspera {
    private static final BlockingQueue<Socket> jugadoresEnEspera = new LinkedBlockingQueue<>();

    public static void agregarJugador(Socket jugador) throws InterruptedException {
        jugadoresEnEspera.put(jugador); // Añade el jugador a la cola
    }

    public static Socket obtenerJugador() throws InterruptedException {
        return jugadoresEnEspera.take(); // Bloquea hasta que haya un jugador disponible
    }

    public static boolean hayJugadoresEsperando() {
        return !jugadoresEnEspera.isEmpty();
    }
}
