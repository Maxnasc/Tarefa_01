package com.example.tarefa_01;

import com.example.calculos.Region;

import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
    private static boolean signal = false;
    private static final Coordinates coordenadas = new Coordinates();
    private static Queue<Region> filaCoordenadas = new LinkedList<>();
    private static boolean adicionarRegiao = false;
    private static int regioesNaFila;
    private static Region regionToEncrypt;
    private Queue<Region> dadosDB = new LinkedList<>();

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

    public Queue<Region> getFilaCoordenadas () {
        return filaCoordenadas;
    }

    public void setFilaCoordenadas (Queue<Region> filaCoordenadas) {
        Semaphore.filaCoordenadas = filaCoordenadas;
    }

    public void setRegionToEncrypt(Region regionToEncrypt) {
        Semaphore.regionToEncrypt = regionToEncrypt;
    }

    public Region getEncryptRequest() {
        return regionToEncrypt;
    }

    public Queue<Region> get_dadosDB() {
        return dadosDB;
    }

    public void set_dadosDB(Queue<Region> dadosDB) {
        this.dadosDB = dadosDB;
    }
}
