package logica;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Representa el horario semanal de atención de la barbería.
 * Convención de días:
 * 0 = Lunes, 1 = Martes, 2 = Miércoles, 3 = Jueves, 4 = Viernes, 5 = Sábado, 6 = Domingo.
 */
public class HorarioAtencion implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String[] NOMBRES_DIAS = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    private boolean[] abierto;
    private int[] horaInicio;
    private int[] horaFin;

    public HorarioAtencion() {
        this.abierto = new boolean[7];
        this.horaInicio = new int[7];
        this.horaFin = new int[7];

        // Horario por defecto:
        // Lunes a Viernes: 8:00 a 18:00
        for (int i = 0; i < 5; i++) {
            this.abierto[i] = true;
            this.horaInicio[i] = 8;
            this.horaFin[i] = 18;
        }
        // Sábado: 8:00 a 16:00
        this.abierto[5] = true;
        this.horaInicio[5] = 8;
        this.horaFin[5] = 16;

        // Domingo: Cerrado
        this.abierto[6] = false;
        this.horaInicio[6] = 0;
        this.horaFin[6] = 0;
    }

    private int normalizarDia(int dia) {
        if (dia >= 0 && dia < 7) {
            return dia;
        }
        // Si se recibe valor 1..7 (estilo java.time.DayOfWeek: 1=Lunes, 7=Domingo)
        if (dia == 7) {
            return 6;
        }
        if (dia > 0 && dia <= 7) {
            return dia - 1;
        }
        return 0;
    }

    public void setDia(int dia, boolean abierto, int inicio, int fin) {
        int d = normalizarDia(dia);
        this.abierto[d] = abierto;
        this.horaInicio[d] = inicio;
        this.horaFin[d] = fin;
    }

    public boolean isDiaAbierto(int dia) {
        int d = normalizarDia(dia);
        return this.abierto[d];
    }

    public int getHoraInicio(int dia) {
        int d = normalizarDia(dia);
        return this.horaInicio[d];
    }

    public int getHoraFin(int dia) {
        int d = normalizarDia(dia);
        return this.horaFin[d];
    }

    /**
     * Valida si la barbería está abierta y en horario de atención para un día y hora específicos.
     * @param dia Día de la semana (0=Lunes .. 6=Domingo, o 1=Lunes .. 7=Domingo)
     * @param hora Hora entera (0 a 23)
     * @return true si está disponible dentro de la jornada, false en caso contrario.
     */
    public boolean estaDisponible(int dia, int hora) {
        int d = normalizarDia(dia);
        if (!this.abierto[d]) {
            return false;
        }
        return hora >= this.horaInicio[d] && hora < this.horaFin[d];
    }

    /**
     * Valida disponibilidad a partir de una fecha concreta.
     */
    public boolean estaDisponible(LocalDate fecha, int hora) {
        if (fecha == null) return false;
        int dia = fecha.getDayOfWeek().getValue() - 1; // 1 (Mon) -> 0, 7 (Sun) -> 6
        return estaDisponible(dia, hora);
    }

    public boolean[] getAbierto() {
        return abierto;
    }

    public int[] getHoraInicio() {
        return horaInicio;
    }

    public int[] getHoraFin() {
        return horaFin;
    }

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
