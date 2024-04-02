package com.example.tarefa_01;

public class RegionObject {
    private String nome;
    private double lat;
    private double longi;
    private int user;
    private long timestamp;

    public RegionObject (String nome, double lat, double longi, int user, long timestamp) {
        this.nome = nome;
        this.lat = lat;
        this.longi = longi;
        this.user = user;
        this.timestamp = timestamp;
    }

    public void setNome (String nome) {
        this.nome = nome;
    }

    public String getNome () {
        return this.nome;
    }

    public double getPosixLatitude () {
        return this.lat;
    }

    public double getPosixLongitude () {
        return this.longi;
    }

    public int getUser () {return this.user;}

    public long getTimestamp () {return timestamp;}

}
