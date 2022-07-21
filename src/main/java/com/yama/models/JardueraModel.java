package com.yama.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class JardueraModel {

    protected int idDB;
    protected String izena;
    protected String mota;
    protected String hasiData;
    protected int distantzia = 0; //m-tan
    protected long iraupena = 0; //s-tan
    protected long denbMugi = 0; //s-tan
    protected double bbAbiadura = 0; //km/h-tan
    protected double abiaduraMax = 0; //km/h-tan
    protected String altueraMin = ""; //m-tan
    protected String altueraMax = ""; //m-tan
    protected String igoeraTot = ""; //m-tan
    protected String bbBihotzMaiz = ""; //bpm-tan
    protected String bihotzMaizMin = ""; //bpm-tan
    protected String bihotzMaizMax = ""; //bpm-tan
    protected String bbKad = ""; //rpm-tan
    protected String kadMax = ""; //rpm-tan
    protected String bbPot = ""; //W-tan
    protected String potMax = ""; //W-tan
    protected String bbTenp = ""; //Cº-tan
    protected String tenpMin = ""; //Cº-tan
    protected String tenpMax = ""; //Cº-tan
    protected ArrayList<Double[]> koordZerr; //[0] latitudea eta [1] longitudea
    protected ArrayList<Double> distZerr; //m-tan
    protected ArrayList<Long> denbMugiZerr; //s-tan
    protected ArrayList<Double> altZerr; //m-tan
    protected ArrayList<Double> abiZerr; //km/h-tan
    protected ArrayList<String> dataZerr;
    protected ArrayList<Integer> bihotzMaizZerr; //bpm-tan
    protected ArrayList<Integer> kadZerr; //rpm-tan
    protected ArrayList<Integer> potZerr; //W-tan
    protected ArrayList<Double> tenpZerr; //Cº-tan

    public JardueraModel(int pIdDB, String pIzena, String pMota, ArrayList<Double[]> pKoordZerr, ArrayList<Double> pAltZerr,
                         ArrayList<String> pDataZerr, ArrayList<Integer> pBihotzMaizZerr, ArrayList<Integer> pKadZerr,
                         ArrayList<Integer> pPotZerr, ArrayList<Double> pTenpZerr) {
        idDB = pIdDB;
        izena = pIzena;
        mota = pMota;

        koordZerr = pKoordZerr;
        distZerr = new ArrayList<>();
        denbMugiZerr = new ArrayList<>();
        altZerr = pAltZerr;
        abiZerr = new ArrayList<>();
        dataZerr = pDataZerr;
        bihotzMaizZerr = pBihotzMaizZerr;
        kadZerr = pKadZerr;
        potZerr = pPotZerr;
        tenpZerr = pTenpZerr;

        kudeatuJarduera();
        kudeatuPenditza();
    }

    private void kudeatuPenditza() {
        //TODO aldapen portzentaiak kontuan izan
    }

    private void kudeatuJarduera() {
        String bukData = "";
        double distDouble = 0;
        String pAzkenAlt = "";
        int pBihotzMaizBatura = 0, pBihotzMaizKop = 0;
        double pTenpBatura = 0; int pTenpKop = 0;
        long pKadBatura = 0; int pKadKop = 0;
        long pPotBatura = 0; int pPotKop = 0;
        int koordErrepKop = 0;

        for (int i = 0; i < koordZerr.size(); i++) {

            //Jardueraren data aurkitzen den lehenengo data izango da
            if (hasiData == null) {
                hasiData = dataZerr.get(i);
            }

            //Jardueraren bukaera data puntu bakoitzarekin eguneratu
            bukData = dataZerr.get(i);

            //Abiadura eta iraupena kudeatu
            if (i > 0) {
                double lat1 = koordZerr.get(i - 1)[0];
                double lon1 = koordZerr.get(i - 1)[1];
                double lat2 = koordZerr.get(i)[0];
                double lon2 = koordZerr.get(i)[1];
                double ele1 = 0.0;
                double ele2 = 0.0;

                BigDecimal bdLat1 = BigDecimal.valueOf(lat1);
                double lat1Bir = bdLat1.setScale(8, RoundingMode.HALF_UP).doubleValue();
                BigDecimal bdLon1 = BigDecimal.valueOf(lon1);
                double lon1Bir = bdLon1.setScale(8, RoundingMode.HALF_UP).doubleValue();
                BigDecimal bdLat2 = BigDecimal.valueOf(lat2);
                double lat2Bir = bdLat2.setScale(8, RoundingMode.HALF_UP).doubleValue();
                BigDecimal bdLon2 = BigDecimal.valueOf(lon2);
                double lon2Bir = bdLon2.setScale(8, RoundingMode.HALF_UP).doubleValue();

                //Aurreko 2 puntuak berdinak diren ala ez egiaztatu
                boolean koordErrep = lat1Bir == lat2Bir && lon1Bir == lon2Bir; //TODO koordenatuen errepikapena altuera kontuan izan gabe kudeatu behar da ere (altuera berriak kalkulatzea falta da)
                if (koordErrep) { //Berdinak badira jarraian dauden errepikatutako puntuen kopurua handitu
                    koordErrepKop++;
                }

                //Azkeneko 2 puntuak berdinak ez badira
                if (!koordErrep) {
                    if (altZerr != null && altZerr.get(i - 1) != null && altZerr.get(i) != null) {
                        ele1 = altZerr.get(i - 1);
                        ele2 = altZerr.get(i);
                    }

                    double puntuDistantzia = distantzia(lat1, lat2, lon1, lon2, ele1, ele2);

                    //Errepikatutako puntuak egon badira, baina aurrekoak bezalakoa ez den puntu bat aurkitzen bada
                    if (koordErrepKop > 0) {

                        //Desberdinak diren azkeneko 2 puntuen arteko abiadura kalkulatu
                        double puntuIraupena;
                        if (i - koordErrepKop - 2 > 0) {
                            puntuIraupena = iraupena(dataZerr.get(i - koordErrepKop - 2), dataZerr.get(i));
                        } else {
                            puntuIraupena = iraupena(dataZerr.get(0), dataZerr.get(i));
                        }
                        double abiaduraMS = 0;
                        if (puntuIraupena > 0) {
                            abiaduraMS = puntuDistantzia / puntuIraupena; //abiadura m/s-tan
                        }
                        double abiaduraKMH = abiaduraMS * 3.6; //m/s-tik km/h-ra pasatzeko

                        /*if (i - koordErrepKop > 1) {
                            abiaduraKMH = (abiaduraKMH + abiZerr.get(i - koordErrepKop - 1)) / 2;

                            //TODO abiadura maximoa kudeatu
                        }*/

                        //Abiadura maximoa kudeatu
                        if (abiaduraKMH > abiaduraMax) {
                            abiaduraMax = abiaduraKMH;
                            if (i > 1) {
                                System.out.println(Arrays.toString(koordZerr.get(i - 1)));
                            }
                            System.out.println(Arrays.toString(koordZerr.get(i)));
                            System.out.println(abiaduraMax);
                            System.out.println();
                        }

                        Double[] koordDifer = {lat2 - lat1, lon2 - lon1}; //Azkeneko 2 koordenatuen arteko diferentzia

                        //Errepikatutako puntuak zeharkatu eta kudeatu
                        for (int j = koordErrepKop; j >= 0; j--) {
                            abiZerr.add(abiaduraKMH);

                            //Denborarekiko ponderazio indizea kalkulatu
                            double azpiPuntIraup = iraupena(dataZerr.get(i - j - 1), dataZerr.get(i - j));
                            double pondInd = azpiPuntIraup / puntuIraupena; //Ponderazio indizea

                            //Denbora mugimenduan kudeatu
                            if (abiaduraKMH > 0.5) {
                                denbMugi += azpiPuntIraup;
                            }
                            denbMugiZerr.add(denbMugi);

                            //Puntuaren distantzia kalkulatu denborarekiko ponderatuz
                            double puntuDistPond = puntuDistantzia * pondInd;
                            distZerr.add(distZerr.get(i - j - 1) + puntuDistPond);
                            distDouble += puntuDistPond;

                            //Koordenatuak eguneratu denborarekiko ponderatuz
                            double latBerriDif = koordDifer[0] * pondInd;
                            double lonBerriDif = koordDifer[1] * pondInd;
                            Double[] aurrekoarenKoord = koordZerr.get(i - j - 1);
                            Double[] koordEguneratuak = {aurrekoarenKoord[0] + latBerriDif, aurrekoarenKoord[1] + lonBerriDif};
                            koordZerr.set(i - j, koordEguneratuak);
                        }
                        koordErrepKop = 0;
                    } else { //Errepikatutako puntuak egon ez badira
                        distZerr.add(distZerr.get(i - 1) + puntuDistantzia); //Distantzia kalkulatu eta gorde

                        //Azkeneko 2 puntuen arteko abiadura kalkulatu
                        double puntuIraupena = iraupena(dataZerr.get(i - 1), dataZerr.get(i));
                        double abiaduraMS = 0;
                        if (puntuIraupena > 0) {
                            abiaduraMS = puntuDistantzia / puntuIraupena; //abiadura m/s-tan
                        }
                        double abiaduraKMH = abiaduraMS * 3.6; //m/s-tik km/h-ra pasatzeko
                        abiZerr.add(abiaduraKMH);

                        //Abiadura maximoa kudeatu
                        if (abiaduraKMH > abiaduraMax) {
                            abiaduraMax = abiaduraKMH;
                        }

                        //Denbora mugimenduan kudeatu
                        if (abiaduraKMH > 0.5) {
                            denbMugi += puntuIraupena;
                        }
                        denbMugiZerr.add(denbMugi);

                        distDouble += puntuDistantzia;
                    }
                } else if (i + 1 == koordZerr.size()) {
                    double azkenDist = distZerr.get(i - koordErrepKop);
                    for (int j = koordErrepKop; j > 0; j--) {
                        distZerr.add(azkenDist);
                        abiZerr.add(0.0);

                        koordErrepKop = 0;
                    }
                }
            } else {
                distZerr.add(0.0);
                denbMugiZerr.add(0L);
                abiZerr.add(0.0);
            }

            //Altuera kudeatu
            if (altZerr != null && altZerr.get(i) != null) {

                //Altuera minimoa kudeatu
                if (altueraMin.isBlank()) {
                    altueraMin = String.valueOf(altZerr.get(i));
                } else if (altZerr.get(i) < Double.parseDouble(altueraMin)) {
                    altueraMin = String.valueOf(altZerr.get(i));
                }

                //Altuera maximoa kudeatu
                if (altueraMax.isBlank()) {
                    altueraMax = String.valueOf(altZerr.get(i));
                } else if (altZerr.get(i) > Double.parseDouble(altueraMax)) {
                    altueraMax = String.valueOf(altZerr.get(i));
                }

                //Igoera kudeatu
                if (pAzkenAlt.isBlank()) {
                    pAzkenAlt = String.valueOf(altZerr.get(i));
                    igoeraTot = String.valueOf(0);
                } else {
                    if (altZerr.get(i) > Double.parseDouble(pAzkenAlt)) {
                        if (igoeraTot.isBlank()) {
                            igoeraTot = String.valueOf(altZerr.get(i) - Double.parseDouble(pAzkenAlt));
                        } else {
                            igoeraTot = String.valueOf(Double.parseDouble(igoeraTot) + (altZerr.get(i) - Double.parseDouble(pAzkenAlt)));
                        }
                    }
                    pAzkenAlt = String.valueOf(altZerr.get(i));
                }
            }

            //Bihotz-maiztasuna kudeatu
            if (bihotzMaizZerr != null && bihotzMaizZerr.get(i) != null) {
                if (bihotzMaizMin.isBlank()) {
                    bihotzMaizMin = String.valueOf(bihotzMaizZerr.get(i));
                } else if (bihotzMaizZerr.get(i) < Double.parseDouble(bihotzMaizMin)) {
                    bihotzMaizMin = String.valueOf(bihotzMaizZerr.get(i));
                }

                if (bihotzMaizMax.isBlank()) {
                    bihotzMaizMax = String.valueOf(bihotzMaizZerr.get(i));
                } else if (bihotzMaizZerr.get(i) > Double.parseDouble(bihotzMaizMax)) {
                    bihotzMaizMax = String.valueOf(bihotzMaizZerr.get(i));
                }

                pBihotzMaizBatura = pBihotzMaizBatura + bihotzMaizZerr.get(i);
                pBihotzMaizKop++;
                bbBihotzMaiz = String.valueOf(pBihotzMaizBatura / pBihotzMaizKop);
            }

            //Kadentzia kudeatu
            if (kadZerr != null && kadZerr.get(i) != null) {
                if (kadMax.isBlank()) {
                    kadMax = String.valueOf(kadZerr.get(i));
                } else if (kadZerr.get(i) > Integer.parseInt(kadMax)) {
                    kadMax = String.valueOf(kadZerr.get(i));
                }

                pKadBatura = pKadBatura + kadZerr.get(i);
                pKadKop++;
                bbKad = String.valueOf(pKadBatura / pKadKop);
            }

            //Potentzia kudeatu
            if (potZerr != null && potZerr.get(i) != null) {
                if (potMax.isBlank()) {
                    potMax = String.valueOf(potZerr.get(i));
                } else if (potZerr.get(i) > Integer.parseInt(potMax)) {
                    potMax = String.valueOf(potZerr.get(i));
                }

                pPotBatura = pPotBatura + potZerr.get(i);
                pPotKop++;
                bbPot = String.valueOf(pPotBatura / pPotKop);
            }

            //Tenperatura kudeatu
            if (tenpZerr != null && tenpZerr.get(i) != null) {
                if (tenpMin.isBlank()) {
                    tenpMin = String.valueOf(tenpZerr.get(i));
                } else if (tenpZerr.get(i) < Double.parseDouble(tenpMin)) {
                    tenpMin = String.valueOf(tenpZerr.get(i));
                }

                if (tenpMax.isBlank()) {
                    tenpMax = String.valueOf(tenpZerr.get(i));
                } else if (tenpZerr.get(i) > Double.parseDouble(tenpMax)) {
                    tenpMax = String.valueOf(tenpZerr.get(i));
                }

                pTenpBatura = pTenpBatura + tenpZerr.get(i);
                pTenpKop++;
                bbTenp = String.valueOf(pTenpBatura / pTenpKop);
            }
        }
        iraupena = iraupena(hasiData, bukData);

        BigDecimal bd = BigDecimal.valueOf(distDouble);
        bd = bd.setScale(0, RoundingMode.HALF_UP);
        distantzia = bd.intValue();

        double bbAbiMS = distDouble / denbMugi;
        bbAbiadura = bbAbiMS * 3.6;
    }

    public int getIdDB() {
        return idDB;
    }

    public void setIdDB(int idDB) {
        this.idDB = idDB;
    }

    public String getIzena() {
        if (izena.isBlank()) {
            return "Kirol jarduera";
        }
        return izena;
    }

    public String getIzenaBal() {
        return izena;
    }

    public void setIzena(String izena) {
        this.izena = izena;
    }

    public String getHasiData() { //Jardueraren data pantailatzerakoan honek izango duen formatua
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date data = sdf.parse(hasiData);
            SimpleDateFormat formatuPantailan = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            return formatuPantailan.format(data);
        } catch (ParseException e) {
            return "?";
        }
    }

    public String getIraupena() { //Jardueraren iraupena pantailatzerakoan honek izango duen formatua
        return formateatuIraupena(iraupena);
    }

    public String getDenbMugi() { //Jardueraren denbora mugimenduan pantailatzerakoan honek izango duen formatua
        return formateatuIraupena(denbMugi);
    }

    public String getBbAbiadura() {
        BigDecimal bd = BigDecimal.valueOf(bbAbiadura);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " km/h";
    }

    public double getBbAbiBal() {
        return bbAbiadura;
    }

    public String getAbiaduraMax() {
        BigDecimal bd = BigDecimal.valueOf(abiaduraMax);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " km/h";
    }

    public double getAbiMaxBal() {
        return abiaduraMax;
    }

    public String getIgoeraTot() {
        BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(igoeraTot));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " m";
    }

    public String getAltueraMin() {
        BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(altueraMin));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " m";
    }

    public double getAltueraMinBal() {
        return Double.parseDouble(altueraMin);
    }

    public String getAltueraMax() {
        BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(altueraMax));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " m";
    }

    public double getAltueraMaxBal() {
        return Double.parseDouble(altueraMax);
    }

    public String getBbBihotzMaiz() {
        return bbBihotzMaiz + " bpm";
    }

    public String getBbBihotzMaizBal() {
        return bbBihotzMaiz;
    }

    public int getBihotzMaizMinBal() {
        return Integer.parseInt(bihotzMaizMin);
    }

    public String getBihotzMaizMax() {
        return bihotzMaizMax + " bpm";
    }

    public int getBihotzMaizMaxBal() {
        return Integer.parseInt(bihotzMaizMax);
    }

    public String getBbTenp() {
        BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(bbTenp));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " ºC";
    }

    public String getBbTenpBal() {
        return bbTenp;
    }

    public String getTenpMin() {
        BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(tenpMin));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " ºC";
    }

    public double getTenpMinBal() {
        return Double.parseDouble(tenpMin);
    }

    public String getTenpMax() {
        BigDecimal bd = BigDecimal.valueOf(Double.parseDouble(tenpMax));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(bd.doubleValue()).replace('.', ',') + " ºC";
    }

    public double getTenpMaxBal() {
        return Double.parseDouble(tenpMax);
    }

    public ArrayList<Double[]> getKoordZerr() {
        return koordZerr;
    }

    public ArrayList<String> getDataZerr() {
        return dataZerr;
    }

    public ArrayList<Double> getAbiZerr() {
        return abiZerr;
    }

    public ArrayList<Double> getDistZerr() {
        return distZerr;
    }

    public ArrayList<Double> getAltZerr() {
        return altZerr;
    }

    public ArrayList<Integer> getBihotzMaizZerr() {
        return bihotzMaizZerr;
    }

    public ArrayList<Double> getTenpZerr() {
        return tenpZerr;
    }

    private String formateatuIraupena(long pDenbora) {
        long denbora = pDenbora;

        long seg = denbora;
        if (seg < 60) {
            return seg + "s";
        }
        seg = seg % 60;
        denbora = denbora - seg;

        long min = denbora / 60;
        if (min < 60) {
            return min + "m " + seg + "s";
        }
        min = min % 60;
        denbora = denbora - (min * 60);

        long ord = denbora / (60 * 60);
        if (ord < 24) {
            return ord + "h " + min + "m " + seg + "s";
        }
        ord = ord % 24;
        denbora = denbora - (ord * 60 * 60);

        long egu = denbora / (60 * 60 * 24);

        return egu + "d " + ord + "h " + min + "m " + seg + "s";
    }

    public String getMota() { //Jardueraren mota pantailatzerakoan honek izango duen formatua
        if (mota.isBlank()) {
            return  "Zehaztu gabeko kirola";
        }
        return mota;
    }

    public String getMotaBal() {
        return mota;
    }

    public void setMota(String mota) {
        this.mota = mota;
    }

    public String getDistantzia() { //Jardueraren distantzia pantailatzerakoan honek izango duen formatua
        if (distantzia < 1000) {
            return distantzia + " m";
        }

        int m = distantzia % 1000;
        if (m >= 100) {
            m = m / 10;
        }
        int km = distantzia / 1000;
        return km + "," + m + " km";
    }

    public double getDistBal() { //Grafikoetan bistaratzeko formatua
        return distantzia;
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

    private long iraupena(String pHasiData, String pBukData) { //Jardueraren iraupena kalkulatu koordenatuak erabiliz
        if (pHasiData != null && pBukData != null && !pHasiData.isBlank() && !pBukData.isBlank()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date hasiData = sdf.parse(pHasiData);
                Date bukData = sdf.parse(pBukData);
                long iraupMilis = bukData.getTime() - hasiData.getTime();

                return iraupMilis / 1000;
            } catch (ParseException ignored) {}
        }

        return 0;
    }

    private String kalkulatuDenbMugimenduan() {
        //TODO kalkulatu hau abiaduraren arabera

        return "";
    }

    private String kudeatuAbiadura() {
        //TODO kalkulatu hau koordenatuen eta elebazioaren arabera

        return "";
    }

    //https://stackoverflow.com/a/16794680
    private double distantzia(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {
        final int R = 6371; //Lurraren erradioa

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
