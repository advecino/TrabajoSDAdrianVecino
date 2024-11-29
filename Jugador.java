import java.net.Socket;

public class Jugador {
    private String nombre;
    private int partidasGanadas;
    private int partidasPerdidas;


    public Jugador(String nombre) {
        this.nombre = nombre;
        this.partidasGanadas = 0;
        this.partidasPerdidas = 0;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    // Método para mostrar la puntuación
    public String getPuntuacion() {
        return nombre + ", tus puntuaciones son: " +
                partidasGanadas + " " + (partidasGanadas == 1 ? "partida ganada" : "partidas ganadas") + " y " +
                partidasPerdidas + " " + (partidasPerdidas == 1 ? "partida perdida" : "partidas perdidas");

    }
}
