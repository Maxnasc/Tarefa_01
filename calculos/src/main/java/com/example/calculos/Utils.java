package com.example.calculos;

import android.location.Location;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    public final double RAIO_REGION = 30.0;
    public final double RAIO_SUB_REGION = 0;
    private static final int MAX_UNIQUE_IDS = 10000;

    public static String getNextUniqueId() {
        Random random = new Random();
        int nextId = random.nextInt(MAX_UNIQUE_IDS);
        // Garantindo que o identificador tenha exatamente 4 dígitos
        return String.format("%04d", nextId);
    }

}
