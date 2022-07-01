package com.yama.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class JardueraModel {

    protected String izena;
    protected String mota;
    protected String hasiData;
    protected String bukData;
    protected int distantzia;
    protected String iraupena;
    protected String denbMugi;
    protected double bbAbiadura;
    protected double abiaduraMax;
    protected int altueraMax;
    protected int bbBM;
    protected int BMMax;
    protected String bbKad;
    protected String kadMax;
    protected double bbtenp;
    protected double tenpMax;
    protected ArrayList<Double[]> koordZerr;
    protected ArrayList<Double> altZerr;
    protected ArrayList<String> dataZerr;
    protected ArrayList<Integer> BMZerr;
    protected ArrayList<Double> tenpZerr;

    public JardueraModel(String pIzena, String pMota, String pHasiData, String pBukData, ArrayList<Double[]> pKoordZerr,
                         ArrayList<Double> pAltZerr, ArrayList<String> pDataZerr, ArrayList<Integer> pBMZerr,
                         ArrayList<Double> pTenpZerr) {
        izena = pIzena;
        mota = pMota;
        hasiData = pHasiData;
        bukData = pBukData;
        iraupena = kalkulatuIraupena(pHasiData, pBukData);
        denbMugi = kalkulatuDenbMugimenduan();

        //TODO metodo bakarrean egin guztia, puntua aldi bakar batean zeharkatzeko
        distantzia = kalkulatuDistantzia(pKoordZerr, pAltZerr);
        //kudeatuAbiadura();
        //kudeatuAltuera();
        //kudeatuBM();
        //kudeatuTenp();

        koordZerr = pKoordZerr;
        altZerr = pAltZerr;
        dataZerr = pDataZerr;
        BMZerr = pBMZerr;
        tenpZerr = pTenpZerr;
    }

    public String getIzena() {
        if (izena.isBlank()) {
            return  "?";
        }
        return izena;
    }

    public String getHasiData() { //Jardueraren data pantailatzerakoan honek izango duen formatua
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date data = sdf.parse(hasiData);
            SimpleDateFormat formatuPantailan = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            return formatuPantailan.format(data);
        } catch (ParseException e) {
            return "?";
        }
    }

    public String getIraupena() { //Jardueraren iraupena pantailatzerakoan honek izango duen formatua
        if (iraupena.isBlank()) {
            return  "?";
        }

        long iraupenLong = Long.parseLong(iraupena);

        long seg = iraupenLong;
        if (seg < 60) {
            return seg + "s";
        }
        seg = seg % 60;
        iraupenLong = iraupenLong - seg;

        long min = iraupenLong / 60;
        if (min < 60) {
            return min + "m " + seg + "s";
        }
        min = min % 60;
        iraupenLong = iraupenLong - (min * 60);

        long ord = iraupenLong / (60 * 60);
        if (ord < 24) {
            return ord + "h " + min + "m " + seg + "s";
        }
        ord = ord % 24;
        iraupenLong = iraupenLong - (ord * 60 * 60);

        long egu = iraupenLong / (60 * 60 * 24);

        return egu + "d " + ord + "h " + min + "m " + seg + "s";
    }

    public String getMota() { //Jardueraren mota pantailatzerakoan honek izango duen formatua
        if (mota.isBlank()) {
            return  "?";
        }
        return mota;
    }

    public String getDistantzia() { //Jardueraren distantzia pantailatzerakoan honek izango duen formatua
        if (distantzia < 1000) {
            return distantzia + "m";
        }

        int m = distantzia % 1000;
        if (m >= 100) {
            m = m / 10;
        }
        int km = distantzia / 1000;
        return km + "," + m + "km";
    }

    private String kalkulatuIraupena(String pHasiData, String pBukData) { //Jardueraren iraupena kalkulatu koordenatuak erabiliz
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date hasiData = sdf.parse(pHasiData);
            Date bukData = sdf.parse(pBukData);
            long iraupMilis = bukData.getTime() - hasiData.getTime();

            return String.valueOf(iraupMilis / 1000);
        } catch (ParseException ignored) {}

        return  "";
    }

    private String kalkulatuDenbMugimenduan() {
        //TODO kalkulatu hau abiaduraren arabera

        return "";
    }

    private String kudeatuAbiadura() {
        //TODO kalkulatu hau koordenatuen eta elebazioaren arabera

        return "";
    }

    //Jardueraren distantzia kalkulatu koordenatuak erabiliz
    private int kalkulatuDistantzia(ArrayList<Double[]> pCoordList, ArrayList<Double> pEleList) {
        double distantzia = 0;

        if (pCoordList != null && pCoordList.size() > 1) { //Jardueran 2 puntu edo gehiago badaude
            Double[] azkenekoPuntua = null;

            //Begizta honetan jardueraren distantzia totala kalkulatzen da puntuen arteko distantzien batura eginez.
            //Puntuen arteko distantziak kalkulatzeko, latitude eta longitude ezagunak dituzten azkeneko 2 puntuak
            //hartzen dira kontuan.
            for (int a = 0, b = 1; b < pCoordList.size(); a++, b++) { //Jardueraren puntu guztiak zeharkatu
                Double[] puntuA = pCoordList.get(a);
                Double[] puntuB = pCoordList.get(b);
                String puntBEle = null;
                String azkPuntEle = null;

                //Latitude eta longitude ezagunak baditu, "puntuA" gorde aztertutako azkeneko puntu bezala
                if (puntuA != null)  {
                    azkenekoPuntua = puntuA;

                    if (pEleList != null && pEleList.get(a) != null) {
                        azkPuntEle = String.valueOf(pEleList.get(a));
                    }
                }

                //Latitude eta longitude ezagunak baditu, "puntuB" aztertu
                if (puntuB != null) {
                    if (pEleList != null && pEleList.get(b) != null) {
                        puntBEle = String.valueOf(pEleList.get(b));
                    }

                    if (azkenekoPuntua != null) { //Distantzia kalkulatu aztertutako punturen bat existitzen bada
                        double lat1 = azkenekoPuntua[0];
                        double lon1 = azkenekoPuntua[1];
                        double lat2 = puntuB[0];
                        double lon2 = puntuB[1];

                        double ele1 = 0.0;
                        double ele2 = 0.0;

                        //Elebazioa erabili distantzia kalkulatzeko bi puntuek elebazio ezaguna badute
                        if (puntBEle != null && azkPuntEle != null) {
                            ele1 = Double.parseDouble(azkPuntEle);
                            ele2 = Double.parseDouble(puntBEle);
                        }

                        distantzia += distantzia(lat1, lat2, lon1, lon2, ele1, ele2);
                    }

                    //"puntuB" gorde aztertutako azkeneko puntua bezala
                    azkenekoPuntua = puntuB;
                }
            }
        }

        BigDecimal bd = BigDecimal.valueOf(distantzia);
        bd = bd.setScale(0, RoundingMode.HALF_UP);

        return bd.intValue();
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
