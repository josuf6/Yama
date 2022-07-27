package com.yama.models;

import java.util.ArrayList;

public class KorrJardModel extends JardueraModel {

    public KorrJardModel(int pIdDB, String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                         ArrayList<String> pDataZerr, ArrayList<Integer> pBihotzMaizZerr, ArrayList<Integer> pKadZerr,
                         ArrayList<Integer> pPotZerr, ArrayList<Double> pTenpZerr) {
        super(pIdDB, pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBihotzMaizZerr, pKadZerr, pPotZerr, pTenpZerr);
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
        int min = (int) (60 / bbAbiadura);
        double minDouble = 60 / bbAbiadura;
        int segInt = (int) ((minDouble - min) * 60);
        String segString = String.valueOf(segInt);
        if (segInt < 10) segString = "0" + segString;
        return min + ":" + segString + " min/km";
    }
}
