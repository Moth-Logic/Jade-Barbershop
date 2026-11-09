package logica;

import java.io.Serializable;
import java.time.LocalDate;

/** Horario semanal de atención de la barbería. */
public class HorarioAtencion implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Nombres de los días de la semana. */
    public static final String[] NOMBRES_DIAS = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    /** Indica para cada día si la barbería abre. */
    private boolean[] abierto;

    /** Hora de apertura de cada día. */
    private int[] horaInicio;

    /** Hora de cierre de cada día. */
    private int[] horaFin;

    /** Crea el horario por defecto de la barbería. */
    public HorarioAtencion() {
        this.abierto = new boolean[7];
        this.horaInicio = new int[7];
        this.horaFin = new int[7];

        // La barbería abre de lunes a viernes de 8:00 a 18:00.
        for (int i = 0; i < 5; i++) {
            this.abierto[i] = true;
            this.horaInicio[i] = 8;
            this.horaFin[i] = 18;
        }

        // El sábado abre de 8:00 a 16:00.
        this.abierto[5] = true;
        this.horaInicio[5] = 8;
        this.horaFin[5] = 16;

        // El domingo permanece cerrado.
        this.abierto[6] = false;
        this.horaInicio[6] = 0;
        this.horaFin[6] = 0;
    }

    /** Comprueba que el índice del día esté entre lunes y domingo. */
    private int normalizarDia(int dia) throws Exception {
        if (dia < 0 || dia > 6) {
            throw new Exception("Invalid day: " + dia + ". Debe ser de 0 (Lunes) a 6 (Domingo).");
        }
        return dia;
    }

    /** Cambia la configuración de un día concreto. */
    public void setDia(int dia, boolean abierto, int inicio, int fin) throws Exception {
        int d = normalizarDia(dia);
        
        if (inicio < 0 || inicio > 23)
            throw new Exception("Invalid start time.");

        if (fin < 0 || fin > 24)
            throw new Exception("Invalid end time.");

        if (abierto && inicio >= fin)
            throw new Exception("Invalid schedule.");
        
        this.abierto[d] = abierto;
        this.horaInicio[d] = inicio;
        this.horaFin[d] = fin;
    }

    /** Indica si la barbería abre ese día. */
    public boolean isDiaAbierto(int dia) throws Exception {
        int d = normalizarDia(dia);
        return this.abierto[d];
    }

    /** Devuelve la hora de apertura de un día. */
    public int getHoraInicio(int dia) throws Exception {
        int d = normalizarDia(dia);
        return this.horaInicio[d];
    }

    /** Devuelve la hora de cierre de un día. */
    public int getHoraFin(int dia) throws Exception {
        int d = normalizarDia(dia);
        return this.horaFin[d];
    }

    /** Indica si la barbería está abierta a esa hora. */
    public boolean estaDisponible(int dia, int hora) throws Exception {
        int d = normalizarDia(dia);
        if (!this.abierto[d]) {
            return false;
        }
        return hora >= this.horaInicio[d] && hora < this.horaFin[d];
    }

    /** Indica si la barbería está abierta en esa fecha y hora. */
    public boolean estaDisponible(LocalDate fecha, int hora) throws Exception {
        if (fecha == null) return false;
        int dia = fecha.getDayOfWeek().getValue() - 1; // Convierte el día de la semana al índice interno.
        return estaDisponible(dia, hora);
    }

    /** Devuelve una copia de los días abiertos. */
    public boolean[] getAbierto() {
        return abierto.clone();
    }

    /** Devuelve una copia de las horas de apertura. */
    public int[] getHoraInicio() {
        return horaInicio.clone();
    }

    /** Devuelve una copia de las horas de cierre. */
    public int[] getHoraFin() {
        return horaFin.clone();
    }

    /** Devuelve el horario de los siete días en varias líneas. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Horario de Atención:\n");
        for (int i = 0; i < 7; i++) {
            sb.append(NOMBRES_DIAS[i]).append(": ");
            if (abierto[i]) {
                sb.append(horaInicio[i]).append(":00 - ").append(horaFin[i]).append(":00\n");
            } else {
                sb.append("Cerrado\n");
            }
        }
        return sb.toString();
    }
}
