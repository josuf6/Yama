package com.yama.models;

import java.util.ArrayList;

public class TxirrJardModel extends JardueraModel {

    //TODO aldapen portzentaiak kontuan izan
    public TxirrJardModel(String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                          ArrayList<String> pDataZerr, ArrayList<Integer> pBihotzMaizZerr, ArrayList<Integer> pKadZerr,
                          ArrayList<Integer> pPotZerr, ArrayList<Double> pTenpZerr) {
        super(pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBihotzMaizZerr, pKadZerr, pPotZerr, pTenpZerr);
    }

    @Override
    public String getIzena() {
        if (izena.isBlank()) {
            return  "Txirrindularitza jarduera";
        }
        return izena;
    }

    public String getBbKad() {
        return bbKad + " rpm";
    }

    public String getBbKadBal() {
        return bbKad;
    }

    public String getKadMax() {
        return kadMax + " rpm";
    }

    public int getKadMaxBal() {
        return Integer.parseInt(kadMax);
    }

    public String getBbPot() {
        return bbPot + " W";
    }

    public String getBbPotBal() {
        return bbPot;
    }

    public String getPotMax() {
        return potMax + " W";
    }

    public int getPotMaxBal() {
        return Integer.parseInt(potMax);
    }

    public ArrayList<Integer> getKadZerr() {
        return kadZerr;
    }

    public ArrayList<Integer> getPotZerr() {
        return potZerr;
    }
}
