package logica;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Horario semanal de atención de la barbería.
 *
 * <p>Convención de días: 0 = Lunes, 1 = Martes, 2 = Miércoles, 3 = Jueves, 4 = Viernes,
 * 5 = Sábado y 6 = Domingo. Las horas son enteras de 0 a 24; la hora de cierre es
 * exclusiva, es decir, si se cierra a las 18:00 no se puede reservar a las 18:00.</p>
 *
 * <p>El horario se guarda junto con el resto de la barbería, por eso la clase es
 * {@link Serializable}.</p>
 */
public class HorarioAtencion implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Nombres de los días en el orden que usa la clase (índice 0 = lunes). */
    public static final String[] NOMBRES_DIAS = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    /** Indica, para cada día, si la barbería abre. */
    private boolean[] abierto;

    /** Hora de apertura de cada día (0 a 23). */
    private int[] horaInicio;

    /** Hora de cierre de cada día (0 a 24, exclusiva). */
    private int[] horaFin;

    /**
     * Crea el horario por defecto: lunes a viernes de 8:00 a 18:00, sábado de 8:00 a
     * 16:00 y domingo cerrado.
     */
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

    /**
     * Comprueba que el día esté en el rango 0 (lunes) a 6 (domingo).
     *
     * @param dia índice del día
     * @return el mismo día, ya validado
     * @throws Exception si el índice está fuera de rango
     */
    private int normalizarDia(int dia) throws Exception {
        if (dia < 0 || dia > 6) {
            throw new Exception("Invalid day: " + dia + ". Debe ser de 0 (Lunes) a 6 (Domingo).");
        }
        return dia;
    }

    /**
     * Cambia la configuración de un día concreto.
     *
     * @param dia     índice del día (0 = lunes ... 6 = domingo)
     * @param abierto {@code true} si la barbería abre ese día
     * @param inicio  hora de apertura (0 a 23)
     * @param fin     hora de cierre (0 a 24); debe ser mayor que {@code inicio} si abre
     * @throws Exception si el día, las horas o el rango son inválidos
     */
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

    /**
     * Indica si la barbería abre ese día.
     *
     * @param dia índice del día (0 = lunes ... 6 = domingo)
     * @return {@code true} si abre
     * @throws Exception si el índice está fuera de rango
     */
    public boolean isDiaAbierto(int dia) throws Exception {
        int d = normalizarDia(dia);
        return this.abierto[d];
    }

    /**
     * Devuelve la hora de apertura de un día.
     *
     * @param dia índice del día (0 = lunes ... 6 = domingo)
     * @return hora de apertura
     * @throws Exception si el índice está fuera de rango
     */
    public int getHoraInicio(int dia) throws Exception {
        int d = normalizarDia(dia);
        return this.horaInicio[d];
    }

    /**
     * Devuelve la hora de cierre de un día.
     *
     * @param dia índice del día (0 = lunes ... 6 = domingo)
     * @return hora de cierre (exclusiva)
     * @throws Exception si el índice está fuera de rango
     */
    public int getHoraFin(int dia) throws Exception {
        int d = normalizarDia(dia);
        return this.horaFin[d];
    }

    /**
     * Valida si la barbería está abierta y en horario de atención para un día y hora
     * específicos.
     *
     * <p>La hora de cierre no se puede reservar: si se cierra a las 18:00, una cita a las
     * 18:00 se rechaza (se compara con {@code hora < horaFin}).</p>
     *
     * @param dia  día de la semana (0 = lunes ... 6 = domingo)
     * @param hora hora entera (0 a 23)
     * @return {@code true} si está disponible dentro de la jornada
     * @throws Exception si el día está fuera de rango
     */
    public boolean estaDisponible(int dia, int hora) throws Exception {
        int d = normalizarDia(dia);
        if (!this.abierto[d]) {
            return false;
        }
        return hora >= this.horaInicio[d] && hora < this.horaFin[d];
    }

    /**
     * Valida disponibilidad a partir de una fecha concreta, convirtiendo el día de la
     * semana de {@link LocalDate} (1 = lunes ... 7 = domingo) al índice interno (0 a 6).
     *
     * @param fecha día concreto
     * @param hora  hora entera (0 a 23)
     * @return {@code true} si la barbería atiende en ese momento
     * @throws Exception si hay un problema al consultar el día
     */
    public boolean estaDisponible(LocalDate fecha, int hora) throws Exception {
        if (fecha == null) return false;
        int dia = fecha.getDayOfWeek().getValue() - 1; // 1 (Mon) -> 0, 7 (Sun) -> 6
        return estaDisponible(dia, hora);
    }

    /**
     * Devuelve una copia del arreglo de días abiertos. Se copia para que nadie pueda
     * modificar el horario saltándose las validaciones de {@link #setDia(int, boolean, int, int)}.
     *
     * @return copia de los 7 indicadores de apertura
     */
    public boolean[] getAbierto() {
        return abierto.clone();
    }

    /**
     * Devuelve una copia de las horas de apertura de la semana.
     *
     * @return copia de las 7 horas de apertura
     */
    public int[] getHoraInicio() {
        return horaInicio.clone();
    }

    /**
     * Devuelve una copia de las horas de cierre de la semana.
     *
     * @return copia de las 7 horas de cierre
     */
    public int[] getHoraFin() {
        return horaFin.clone();
    }

    /**
     * Resume el horario en varias líneas, útil para mensajes y registros.
     *
     * @return texto con el horario de los siete días
     */
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
