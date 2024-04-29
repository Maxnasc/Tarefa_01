package com.example.calculos;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class SubRegion extends Region {
    private Region mainRegion;
    public final double RAIO_MINIMO = 0;

    public SubRegion() {}

    public SubRegion (String nome, double lat, double longi, int user, long timestamp, Region mainRegion) {
        super(nome, lat, longi, user, timestamp);
        this.mainRegion = mainRegion;
    }

    @Override
    public Map<String, Object> getAllDataForDatabase() {
        Map<String, Object> data = new HashMap<>(super.getAllDataForDatabase());
        data.put("mainRegion", this.mainRegion);
        return data;
    }

    @Override
    public boolean verifyDistance(Queue<Region> dadosDB) {
        double RAIO_MINIMO = 0; //Raio para subRegiões e regiões restritas
        double menorDistancia = Double.MAX_VALUE;
        float[] results = new float[1];

        for (Region coord : dadosDB) {
            Location.distanceBetween(this.posixLatitude, this.posixLongitude, coord.getPosixLatitude(), coord.getPosixLongitude(), results);
            if (results[0] < menorDistancia) {
                menorDistancia = results[0];
            }
        }
        if (menorDistancia < RAIO_MINIMO){
            return false;
        } else {
            return true;
        }
    }
}
