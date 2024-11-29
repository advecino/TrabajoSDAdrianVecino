import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

public class Pregunta implements Callable<String> {
    private PrintWriter salida;
    private BufferedReader entrada;

    public Pregunta(PrintWriter salida, BufferedReader entrada) {
        this.salida = salida;
        this.entrada = entrada;
    }

    @Override
    public String call() {
        salida.println("¿Quieres jugar contra la máquina (1) o contra otro jugador (2)?");
        String opcion = null;
        try {
            opcion = entrada.readLine();
            if (opcion != null) {
                System.out.println("Cliente eligió: " + opcion);
            } else {
                System.out.println("Cliente se desconectó antes de responder.");
            }
        } catch (IOException e) {
            System.err.println("Error al leer la opción del cliente: " + e.getMessage());
        }
        return opcion;
    }
}
