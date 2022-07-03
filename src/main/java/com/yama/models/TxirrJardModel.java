package com.yama.models;

import java.util.ArrayList;

public class TxirrJardModel extends JardueraModel {

    //TODO aldapen portzentaiak kontuan izan
    private String bbKad;
    private String kadMax;
    private String bbPot;
    private String potMax;
    private ArrayList<Integer> kadZerr;
    private ArrayList<Integer> potZerr;

    public TxirrJardModel(String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                          ArrayList<String> pDataZerr, ArrayList<Integer> pBMZerr,
                          ArrayList<Double> pTenpZerr, ArrayList<Integer> pKadZerr, ArrayList<Integer> pPotZerr) {
        super(pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBMZerr, pTenpZerr);

        kadZerr = pKadZerr;
        potZerr = pPotZerr;
        kudeatuKadentzia();
        kudeatuPotentzia();
    }

    private void kudeatuKadentzia() { //Batez besteko kadentzia eta kadentzia maximoak kalkulatu
        ArrayList<Integer> pKadZerr = kadZerr;
        if (pKadZerr != null) {
            int pKadMax = 0;
            long kadBatura = 0;
            int kadKop = 0;
            for (int i = 0; i < pKadZerr.size(); i++) {
                if (pKadZerr.get(i) != null && pKadZerr.get(i) > 0) {
                    kadBatura = kadBatura + pKadZerr.get(i);
                    kadKop++;

                    if (pKadZerr.get(i) > pKadMax) {
                        pKadMax = pKadZerr.get(i);
                    }
                }
            }

            if (kadKop > 0) {
                bbKad = String.valueOf(kadBatura / kadKop);
                kadMax = String.valueOf(pKadMax);
            } else {
                bbKad = "";
                kadMax = "";
            }
        } else {
            bbKad = "";
            kadMax = "";
        }
    }

    private void kudeatuPotentzia() { //Batez besteko potentzia eta potentzia maximoak kalkulatu
        ArrayList<Integer> pPotZerr = potZerr;
        if (pPotZerr != null) {
            int pPotMax = 0;
            long potBatura = 0;
            int potKop = 0;
            for (int i = 0; i < pPotZerr.size(); i++) {
                if (pPotZerr.get(i) != null && pPotZerr.get(i) > 0) {
                    potBatura = potBatura + pPotZerr.get(i);
                    potKop++;

                    if (pPotZerr.get(i) > pPotMax) {
                        pPotMax = pPotZerr.get(i);
                    }
                }
            }

            if (potKop > 0) {
                bbPot = String.valueOf(potBatura / potKop);
                potMax = String.valueOf(pPotMax);
            } else {
                bbPot = "";
                potMax = "";
            }
        } else {
            bbPot = "";
            potMax = "";
        }
    }
}
