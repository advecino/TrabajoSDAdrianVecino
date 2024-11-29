// Clase para manejar los deatos de los jugadores

public class Jugador {
    private final String nombre;
    private int partidasGanadas;
    private int partidasPerdidas;


    public Jugador(String nombre) {
        this.nombre = nombre;
        this.partidasGanadas = 0;
        this.partidasPerdidas = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    public void setPartidasGanadas(int partidasGanadas) {
        this.partidasGanadas = partidasGanadas;
    }

    public int getPartidasPerdidas() {
        return partidasPerdidas;
    }

    public void setPartidasPerdidas(int partidasPerdidas) {
        this.partidasPerdidas = partidasPerdidas;
    }

    // Métodos para incrementar partidas ganadas y perdidas
    public void ganarPartida() {
        this.partidasGanadas++;
    }

    public void perderPartida() {
        this.partidasPerdidas++;
    }

    // Mostrar la puntuación
    public String getPuntuacion() {
        return nombre + ", tus puntuaciones son: " +
                partidasGanadas + " " + (partidasGanadas == 1 ? "partida ganada" : "partidas ganadas") + " y " +
                partidasPerdidas + " " + (partidasPerdidas == 1 ? "partida perdida" : "partidas perdidas");

    }
}
