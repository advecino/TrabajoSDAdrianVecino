// Autor: Adrián Vecino Durán

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ServidorAhorcado {
    private static final int PUERTO = 12345;
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            while (!Thread.interrupted()) {
            Socket socket = serverSocket.accept();
            pool.execute(new GestionUsuarios(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
