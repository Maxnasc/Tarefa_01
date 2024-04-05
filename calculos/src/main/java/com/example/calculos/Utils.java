package com.example.calculos;

import android.location.Location;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    public final double RAIO = 30.0;
    private static final int MAX_UNIQUE_IDS = 10000;

    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    public static String getNextUniqueId() {
        Random random = new Random();
        int nextId = random.nextInt(MAX_UNIQUE_IDS);
        // Garantindo que o identificador tenha exatamente 4 dígitos
        return String.format("%04d", nextId);
    }

}
