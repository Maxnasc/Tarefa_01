package com.example.calculos;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class RestrictedRegion extends Region {
    private Region mainRegion;
    private boolean restricted;

    public RestrictedRegion() {}
   public RestrictedRegion (String nome, double lat, double longi, int user, long timestamp, Region mainRegion) {
       super(nome, lat, longi, user, timestamp);
       this.mainRegion = mainRegion;
       this.restricted = true;
    }

    public Region getMainRegion() {
        return mainRegion;
    }

    public void setMainRegion(Region mainRegion) {
        this.mainRegion = mainRegion;
    }

    public boolean getRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    @Override
    public Map<String, Object> getAllDataForDatabase() {
        Map<String, Object> data = new HashMap<>(super.getAllDataForDatabase());
        data.put("mainRegion", this.mainRegion);
        data.put("Restricted", this.restricted);
        return data;
    }

    @Override
    public boolean verifyDistance(Queue<Region> dadosDB) {
        double RAIO_MINIMO = 5; //Raio para subRegiões e regiões restritas
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
