package com.yama.models;

import java.util.ArrayList;

public class IbilJardModel extends JardueraModel {

    public IbilJardModel(String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                         ArrayList<String> pDataZerr, ArrayList<Integer> pBMZerr, ArrayList<Double> pTenpZerr) {
        super(pIzena, pMota, pKoordZerr, pAltZerr, pDataZerr, pBMZerr, pTenpZerr);
    }
}
