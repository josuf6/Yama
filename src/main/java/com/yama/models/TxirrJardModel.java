package com.yama.models;

import java.util.ArrayList;

public class TxirrJardModel extends JardueraModel {

    //TODO aldapen portzentaiak kontuan izan
    public TxirrJardModel(int pIdDB, String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                          ArrayList<String> pDataZerr, ArrayList<Integer> pBihotzMaizZerr, ArrayList<Integer> pKadZerr,
                          ArrayList<Integer> pPotZerr, ArrayList<Double> pTenpZerr) {
        super(pIdDB, pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBihotzMaizZerr, pKadZerr, pPotZerr, pTenpZerr);
    }

    @Override
    public String getIzena() {
        if (izena.isBlank()) {
            return  "Txirrindularitza jarduera";
        }
        return izena;
    }
}
