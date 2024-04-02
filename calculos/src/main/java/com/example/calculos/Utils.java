package com.example.calculos;

import android.location.Location;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    // Método para calcular a distância entre duas coordenadas geográficas usando a fórmula de Haversine
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    public static long getNextUniqueId() {
        AtomicInteger idCounter = new AtomicInteger();
        return idCounter.incrementAndGet();
    }
}
