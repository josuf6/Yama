package com.yama.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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

    @Override
    public String getBbAbiadura() {
        BigDecimal bd = BigDecimal.valueOf(bbAbiadura / 3.6);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " m/s";
    }
}
