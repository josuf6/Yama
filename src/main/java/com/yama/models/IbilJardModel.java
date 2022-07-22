package com.yama.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class IbilJardModel extends JardueraModel {

    public IbilJardModel(int pIdDB, String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                         ArrayList<String> pDataZerr, ArrayList<Integer> pBihotzMaizZerr, ArrayList<Integer> pKadZerr,
                         ArrayList<Integer> pPotZerr, ArrayList<Double> pTenpZerr) {
        super(pIdDB, pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBihotzMaizZerr, pKadZerr, pPotZerr, pTenpZerr);
    }

    @Override
    public String getIzena() {
        if (izena.isBlank()) {
            return  "Ibilaritza jarduera";
        }
        return izena;
    }

    @Override
    public String getBbAbiadura() {
        //TODO hau aldatu (errtimoa denbora/distantzia da)
        BigDecimal bd = BigDecimal.valueOf(bbAbiadura / 3.6);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " m/s";
    }
}
