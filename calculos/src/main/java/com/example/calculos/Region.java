package com.example.calculos;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Region {
    protected String nome;
    protected double posixLatitude;
    protected double posixLongitude;
    protected int user;
    protected long timestamp;
    protected Map<String, String> dadoEncriptado;


    public Region() {

    }

    public Region(String nome, double lat, double longi, int user, long timestamp) {
        this.nome = nome;
        this.posixLatitude = lat;
        this.posixLongitude = longi;
        this.user = user;
        this.timestamp = timestamp;
    }

    public void setDadoEncriptado (Map<String, String> dadoEncriptado) {
        this.dadoEncriptado = dadoEncriptado;
    }

    public Map<String, String> getDadoEncriptado() {
        return dadoEncriptado;
    }

    public String getNome () {
        return this.nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPosixLatitude () {
        return this.posixLatitude;
    }

    public void setPosixLatitude (Double posixLatitude) {
        this.posixLatitude = posixLatitude;
    }

    public double getPosixLongitude () {
        return this.posixLongitude;
    }
    public void setPosixLongitude (Double posixLongitude) {
        this.posixLongitude = posixLongitude;
    }

    public int getUser () {return this.user;}

    public void setUser (int user) {
        this.user = user;
    }

    public long getTimestamp () {return timestamp;}

    public void setTimestamp (long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean verifyDistance(Queue<Region> dadosDB) {
        double RAIO_MINIMO = 30.0; //Raio para regiões
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

    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    // Método que retorna todos os dados necessários para salvar no banco de dados
    public Map<String, Object> getAllDataForDatabase() {
        Map<String, Object> data = new HashMap<>();
        data.put("nome", this.nome);
        data.put("posixLatitude", this.posixLatitude);
        data.put("posixLongitude", this.posixLongitude);
        data.put("timestamp", this.timestamp);
        data.put("user", this.user);
        return data;
    }


}
