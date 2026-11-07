package com.example.sleeptrackapp;

import java.util.Locale;

/**
 * Utilidades para el cálculo de métricas de descanso al estilo Sleep Cycle.
 * Maneja transiciones de medianoche, cálculo de calidad de sueño basada en sensores
 * (acelerómetro y duración) y estimación de eficiencia.
 */
public class SleepTimeUtils {

    /**
     * Calcula la diferencia en horas y minutos entre hora de dormir y hora de despertar.
     * @param horaDormirStr Formato HH:mm (ej. "00:00", "23:30")
     * @param horaDespertarStr Formato HH:mm (ej. "09:00", "07:00")
     * @return Arreglo de 2 enteros: [horas, minutos]
     */
    public static int[] calcularHorasYMinutos(String horaDormirStr, String horaDespertarStr) {
        int h1 = 23, m1 = 0;
        int h2 = 7, m2 = 0;

        try {
            if (horaDormirStr != null) {
                String[] p1 = horaDormirStr.split(":");
                if (p1.length == 2) {
                    h1 = Integer.parseInt(p1[0].trim());
                    m1 = Integer.parseInt(p1[1].trim());
                }
            }
        } catch (Exception ignored) {}

        try {
            if (horaDespertarStr != null) {
                String[] p2 = horaDespertarStr.split(":");
                if (p2.length == 2) {
                    h2 = Integer.parseInt(p2[0].trim());
                    m2 = Integer.parseInt(p2[1].trim());
                }
            }
        } catch (Exception ignored) {}

        int min1 = h1 * 60 + m1;
        int min2 = h2 * 60 + m2;
        int diff = min2 - min1;

        if (diff <= 0) {
            diff += 24 * 60;
        }

        int horas = diff / 60;
        int minutos = diff % 60;
        return new int[]{horas, minutos};
    }

    /**
     * Algoritmo estilo Sleep Cycle: Calcula el porcentaje de calidad de sueño (%)
     * combinando el cumplimiento de la meta de tiempo y la quietud capturada por el acelerómetro.
     */
    public static int calcularCalidadPorcentaje(int duracionMinutosReales, int duracionMinutosMeta, int movimientosAcelerometro) {
        if (duracionMinutosReales <= 1) return 0;
        if (duracionMinutosMeta <= 0) duracionMinutosMeta = 480; // 8 hrs por defecto

        // Cumplimiento base de tiempo
        float ratioTiempo = (float) duracionMinutosReales / duracionMinutosMeta;
        if (ratioTiempo > 1.0f) ratioTiempo = 1.0f;
        float baseCalidad = ratioTiempo * 100.0f;

        // Penalización por agitación/movimiento capturado por acelerómetro (cada movimiento descuenta ~2%)
        float penalizacionMovimiento = movimientosAcelerometro * 2.0f;

        int resultado = Math.round(baseCalidad - penalizacionMovimiento);
        if (resultado > 100) resultado = 100;
        if (resultado < 0) resultado = 0;

        return resultado;
    }

    /**
     * Clasifica el porcentaje de calidad estilo Sleep Cycle.
     */
    public static String clasificarCalidad(int porcentaje) {
        if (porcentaje >= 85) return "Muy buena";
        if (porcentaje >= 70) return "Buena";
        if (porcentaje >= 50) return "Regular";
        if (porcentaje >= 25) return "Mala";
        return "Muy mala";
    }

    /**
     * Estima la Eficiencia de Sueño (%) en función del tiempo de reposo y movimientos.
     */
    public static int calcularEficiencia(int duracionMinutosReales, int movimientosAcelerometro) {
        if (duracionMinutosReales <= 1) return 0;
        int base;
        if (duracionMinutosReales < 30) {
            base = Math.round((duracionMinutosReales / 30.0f) * 70.0f);
        } else {
            base = 95;
        }
        int eficiencia = base - (movimientosAcelerometro * 2);
        if (eficiencia > 99) eficiencia = 99;
        if (eficiencia < 0) eficiencia = 0;
        return eficiencia;
    }

    public static String formatearDuracionTexto(int horas, int minutos) {
        if (minutos == 0) {
            return horas == 1 ? "1 hora de descanso" : horas + " horas de descanso";
        } else {
            String horasStr = (horas == 1) ? "1 hora" : horas + " horas";
            return horasStr + " " + minutos + " min de descanso";
        }
    }

    public static String formatearDuracionCorto(int horas, int minutos) {
        return String.format(Locale.getDefault(), "%dh %02dm", horas, minutos);
    }
}