package com.yama.models;

public class ErabiltzaileModel {

    private String ezizena;
    private String izena;
    private String abizena;

    public ErabiltzaileModel(String pEzizena, String pIzena, String pAbizena) {
        ezizena = pEzizena;
        izena = pIzena;
        abizena = pAbizena;
    }

    public String getEzizena() {
        return ezizena;
    }

    public String getIzena() {
        return izena;
    }

    public String getAbizena() {
        return abizena;
    }
}
