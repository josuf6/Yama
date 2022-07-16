package com.yama.models;

public class ErabiltzaileModel {

    private String ezizena;
    private String izena;
    private String abizena;

    public ErabiltzaileModel (String pEzizena, String pIzena, String pAbizena) {
        ezizena = pEzizena;
        izena = pIzena;
        abizena = pAbizena;
    }

    public String getEzizena() {
        return ezizena;
    }

    public void setEzizena(String ezizena) {
        this.ezizena = ezizena;
    }

    public String getIzena() {
        return izena;
    }

    public void setIzena(String izena) {
        this.izena = izena;
    }

    public String getAbizena() {
        return abizena;
    }

    public void setAbizena(String abizena) {
        this.abizena = abizena;
    }
}
