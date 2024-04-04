package com.example.tarefa_01;

public class RegionObject {
    private String nome;
    private double posixLatitude;
    private double posixLongitude;
    private int user;
    private long timestamp;

    public RegionObject () {

    }

    public RegionObject (String nome, double lat, double longi, int user, long timestamp) {
        this.nome = nome;
        this.posixLatitude = lat;
        this.posixLongitude = longi;
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
        return this.posixLatitude;
    }

    public double getPosixLongitude () {
        return this.posixLongitude;
    }

    public int getUser () {return this.user;}

    public long getTimestamp () {return timestamp;}

}
