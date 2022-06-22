package com.yama.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JardueraModel {

    private String name;
    private String type;
    private String time;
    private String endTime;
    private String distance;
    private String duration;
    private String movingTime;
    private JSONArray points;

    public JardueraModel(String pName, String pType, String pTime, String pEndTime, JSONArray pPoints) {
        name = pName;
        time = pTime;
        endTime = pEndTime;
        type = kalkulatuJardMota(pType);
        points = pPoints;

        String[] dataFormatuak = new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
        duration = kalkulatuIraupena(dataFormatuak);
        movingTime = kalkulatuDenbMugimenduan(dataFormatuak);
        distance = kalkulatuDistantzia();
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        //TODO aldatu pantailan erakusten den dataren formatua

        return time;
    }

    public String getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }

    public String getDistance() {
        return distance;
    }

    private String kalkulatuJardMota(String pType) {
        //TODO Kalkulatu jarduera mota "pType"-ren balioaren arabera

        return "";
    }

    private String kalkulatuIraupena(String[] dataFormatuak) {
        for (String formatu : dataFormatuak) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(formatu);
                Date hasiData = sdf.parse(time);
                Date bukData = sdf.parse(endTime);
                long iraupMilis = bukData.getTime() - hasiData.getTime();

                long seg = iraupMilis / 1000;
                if (seg < 60) {
                    return seg + "s";
                }
                seg = seg % 60;
                iraupMilis = iraupMilis - seg * 1000;

                long min = iraupMilis / (1000 * 60);
                if (min < 60) {
                    return min + "m " + seg + "s";
                }
                min = min % 60;
                iraupMilis = iraupMilis - min * 1000 * 60;

                long ord = iraupMilis / (1000 * 60 * 60);
                if (ord < 24) {
                    return ord + "h " + min + "m " + seg + "s";
                }
                ord = ord % 24;
                iraupMilis = iraupMilis - ord * 1000 * 60 * 60;

                long egu = iraupMilis / (1000 * 60 * 60 * 24);

                return egu + "d " + ord + "h " + min + "m " + seg + "s";
            } catch (ParseException ignored) {}
        }

        return "";
    }

    private String kalkulatuDenbMugimenduan(String[] dataFormatuak) {
        //TODO kalkulatu hau abiaduraren arabera

        return "";
    }

    private String kalkulatuDistantzia() {
        double distantzia = 0;

        if (!points.isEmpty() && points.length() > 1) { //Jardueran 2 puntu edo gehiago badaude
            JSONObject azkenekoPuntua = null;

            //Begizta honetan jardueraren distantzia totala kalkulatzen da puntuen arteko distantzien batura eginez.
            //Puntuen arteko distantziak kalkulatzeko, latitude eta longitude ezagunak dituzten azkeneko 2 puntuak
            //hartzen dira kontuan.
            for (int a = 0, b = 1; b < points.length(); a++, b++) { //Jardueraren puntu guztiak zeharkatu
                JSONObject puntuA = points.getJSONObject(a);
                JSONObject puntuB = points.getJSONObject(b);

                //Latitude eta longitude ezagunak baditu, "puntuA" gorde aztertutako azkeneko puntu bezala
                if (puntuA.has("latitude") && puntuA.has("longitude"))  {
                    azkenekoPuntua = puntuA;
                }

                //Latitude eta longitude ezagunak baditu, "puntuB" aztertu
                if (puntuB.has("latitude") && puntuB.has("longitude")) {
                    if (azkenekoPuntua != null) { //Distantzia kalkulatu aztertutako punturen bat existitzen bada
                        double lat1 = Double.parseDouble(azkenekoPuntua.getString("latitude"));
                        double lon1 = Double.parseDouble(azkenekoPuntua.getString("longitude"));
                        double lat2 = Double.parseDouble(puntuB.getString("latitude"));
                        double lon2 = Double.parseDouble(puntuB.getString("longitude"));

                        double ele1 = 0.0;
                        double ele2 = 0.0;

                        //Elebazioa erabili distantzia kalkulatzeko bi puntuek elebazio ezaguna badute
                        if (puntuB.has("elevation") && azkenekoPuntua.has("elevation")) {
                            ele1 = Double.parseDouble(azkenekoPuntua.getString("elevation"));
                            ele2 = Double.parseDouble(puntuB.getString("elevation"));
                        }

                        distantzia += distantzia(lat1, lat2, lon1, lon2, ele1, ele2) / 1000;
                    }

                    //"puntuB" gorde aztertutako azkeneko puntua bezala
                    azkenekoPuntua = puntuB;
                }
            }
        }

        BigDecimal bd = BigDecimal.valueOf(distantzia);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return String.valueOf(bd.doubleValue());
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
