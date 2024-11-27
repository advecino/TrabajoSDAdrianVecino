import java.net.Socket;

public class Jugador {
    private String nombre;
    private Socket socket;
    private int partidasGanadas;
    private int partidasPerdidas;

    // Constructor
    public Jugador(String nombre, Socket socket) {
        this.nombre = nombre;
        this.socket = socket;
        this.partidasGanadas = 0;
        this.partidasPerdidas = 0;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    public int getPartidasPerdidas() {
        return partidasPerdidas;
    }

    // Métodos para incrementar partidas ganadas y perdidas
    public void ganarPartida() {
        this.partidasGanadas++;
    }

    public void perderPartida() {
        this.partidasPerdidas++;
    }
}
