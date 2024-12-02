import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class ServidorAhorcado {
    private static final int PUERTO = 12345;
    private static ExecutorService pool = Executors.newCachedThreadPool(); // Para manejar hilos de partidas
    private static List<Socket> clientes = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            while (!Thread.currentThread().isInterrupted()) {
            Socket socket = serverSocket.accept();

            pool.execute(new GestionUsuarios(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
