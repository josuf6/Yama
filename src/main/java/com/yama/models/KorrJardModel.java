package com.yama.models;

import java.util.ArrayList;

public class KorrJardModel extends JardueraModel {

    public KorrJardModel(String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                         ArrayList<String> pDataZerr, ArrayList<Integer> pBMZerr, ArrayList<Double> pTenpZerr) {
        super(pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBMZerr, pTenpZerr);
    }

    @Override
    public String getIzena() {
        if (izena.isBlank()) {
            return  "Korrika jarduera";
        }
        return izena;
    }
}
