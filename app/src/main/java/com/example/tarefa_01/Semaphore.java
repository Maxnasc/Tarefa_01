package com.example.tarefa_01;

import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
    private static boolean signal = false;
    private static final Coordinates coordenadas = new Coordinates();
    private static Queue<RegionObject> filaCoordenadas = new LinkedList<>();
    private static boolean adicionarRegiao = false;
    private static int regioesNaFila;

    public synchronized void take() {
        signal = true;
        this.notify();
    }

    public synchronized void release() throws InterruptedException {
        while (!signal) wait();
        signal = false;
    }

    public void set_coordenadas (double latitude, double longitude) {
        coordenadas.setLatitude(latitude);
        coordenadas.setLongitude(longitude);
    }

    public Coordinates get_coordenadas () {
        return coordenadas;
    }

    public void requestRegion() {
        adicionarRegiao = true;
    }

    public boolean getRequest() {
        boolean adicionarRegiaoAux = adicionarRegiao;
        adicionarRegiao = false; // Volta o status para não lido
        return adicionarRegiaoAux;
    }

    public void setNumberRegionsOnQueue(int numberRegions) {
        this.regioesNaFila = numberRegions;
    }

    public String getNumberRegionsOnQueue() {
        return String.valueOf(regioesNaFila);
    }

    public Queue<RegionObject> getFilaCoordenadas () {
        return filaCoordenadas;
    }

    public void setFilaCoordenadas (Queue<RegionObject> filaCoordenadas) {
        Semaphore.filaCoordenadas = filaCoordenadas;
    }
}
