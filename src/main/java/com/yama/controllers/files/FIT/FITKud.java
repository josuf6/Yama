package com.yama.controllers.files.FIT;

import com.google.common.io.Files;
import com.yama.models.IbilJardModel;
import com.yama.models.JardueraModel;
import com.yama.models.KorrJardModel;
import com.yama.models.TxirrJardModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class FITKud {

    private static final FITKud nFITKud = new FITKud();

    public static FITKud getFITKud() {
        return nFITKud;
    }

    private FITKud() {}

    public JardueraModel kudeatuFIT(File fitx) {

        //FIT fitxategiaren edukian oinarritutako CSV fitxategi bat sortu
        String tmpdir = System.getProperty("java.io.tmpdir");
        String fitxIzena = Files.getNameWithoutExtension(fitx.getName());
        String csvFitxPath = tmpdir + fitxIzena + ".csv";

        CSVTool csvTool = new CSVTool();
        csvTool.run(new String[]{"-b", fitx.getAbsolutePath(), csvFitxPath});

        //Sortutako fitxategia parseatu eta gorde lortutako datuak JardueraModel objektu batean
        JardueraModel jarduera = parseatuCSV(csvFitxPath);

        //Sortutako CSV fitxategia ezabatu
        File myObj = new File(csvFitxPath);
        myObj.delete();

        return jarduera;
    }

    private JardueraModel parseatuCSV(String csvFitxPath) {
        int typePos = -1;
        int messagePos = -1;
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(csvFitxPath));

            //Fitxategiaren lehenengo lerroa kudeatu
            String line = reader.readLine();
            String[] firstLineList = line.split(",");
            for (int i = 0; i < firstLineList.length; i++) {
                String lineElem = firstLineList[i].replaceAll("\\P{Print}","");
                if (lineElem.equals("Type")) {
                    typePos = i;
                } else if (lineElem.equals("Message")) {
                    messagePos = i;
                }
            }

            //Fitxategiaren gainerako lerroak kudeatu lehenengo lerroan "Type" eta "Message" aurkitu badira
            if (typePos > -1 && messagePos > -1) {
                int fitxMota = -1; //Aztertutako fitxategia zein FIT motakoa den jakiteko

                String jardIzena = ""; //FIT Activity ez da jardueraren izenik gordetzen
                String jardMota = "";

                //Jardueraren puntu bakoitzean aztertuko diren informaziorako zerrendak
                ArrayList<Double[]> coordZerr = new ArrayList<>();
                ArrayList<Double> eleZerr = new ArrayList<>();
                ArrayList<String> timeZerr = new ArrayList<>();
                ArrayList<Integer> hrZerr = new ArrayList<>();
                ArrayList<Double> tempZerr = new ArrayList<>();
                ArrayList<Integer> cadZerr = new ArrayList<>();
                ArrayList<Integer> pwZerr = new ArrayList<>();

                //CSVaren lerroak bukatu arte edo fitxMota > 0 eta != 4 den arte aztertu
                //FitxMota = 4 diren fitxategiak "Activity" motakoak dira, eta hauek aztertu nahi dira bakarrik
                while (line != null && (fitxMota < 0 || fitxMota == 4)) {
                    String[] lineList = line.split(",");
                    String type = lineList[typePos];
                    String message = lineList[messagePos];

                    if (type.equals("Data") && message.equals("file_id")) { //FIT fitxategi mota lortzeko
                        for (int i = 3 ; i < lineList.length; i = i + 3) {
                            if (lineList[i].equals("type")) {
                                fitxMota = Integer.parseInt(lineList[i + 1].replace("\"", ""));
                            }
                        }
                    } else if (type.equals("Data") && message.equals("session")) { //Kirol mota lortzeko
                        for (int i = 3 ; i < lineList.length; i = i + 3) {
                            if (lineList[i].equals("sport")) {
                                int jardMotaInt = Integer.parseInt(lineList[i + 1].replace("\"", ""));

                                if (jardMotaInt == 2 || jardMotaInt == 21) { //Txirrindularitza jarduera
                                    jardMota = "Txirrindularitza";
                                } else if (jardMotaInt == 1) { //Korrika jarduera
                                    jardMota = "Korrika";
                                } else if (jardMotaInt == 11 || jardMotaInt == 17) { //Ibilaritza jarduera
                                    jardMota = "Ibilaritza";
                                }
                            }
                        }
                    } else if (type.equals("Data") && message.equals("record")) { //Puntuen informazioa lortzeko
                        String lat = "", lon = "", time = "", ele = "", hr = "", temp = "", cad = "", power = "";

                        for (int i = 3 ; i < lineList.length; i = i + 3) {

                            //Puntuaren koordenatuak lortu
                            if (lineList[i].equals("position_lat") && lineList[i + 2].equals("semicircles")) {
                                String latSemicircles = lineList[i + 1].replace("\"", "");
                                lat = String.valueOf(Double.parseDouble(latSemicircles) * (180.0 / Math.pow(2, 31)));
                            }
                            if (lineList[i].equals("position_long") && lineList[i + 2].equals("semicircles")) {
                                String lonSemicircles = lineList[i + 1].replace("\"", "");
                                lon = String.valueOf(Double.parseDouble(lonSemicircles) * (180.0 / Math.pow(2, 31)));
                            }

                            //Puntuaren denbora (data eta ordua) lortu
                            if (lineList[i].equals("timestamp") && lineList[i + 2].equals("s")) {
                                long timestampSecs = Integer.parseInt(lineList[i + 1].replace("\"", "")) + 631065600;
                                long timestampMilis = timestampSecs * 1000;
                                time = kalkulatuData(String.valueOf(timestampMilis));
                            }

                            //Puntuaren elebazioa lortu
                            if (lineList[i].equals("altitude") && lineList[i + 2].equals("m")) {
                                ele = lineList[i + 1].replace("\"", "");
                            }

                            //Puntuaren bihotz-maiztasuna lortu
                            if (lineList[i].equals("heart_rate") && lineList[i + 2].equals("bpm")) {
                                hr = lineList[i + 1].replace("\"", "");
                            }

                            //Puntuaren tenperatura lortu
                            if (lineList[i].equals("temperature") && lineList[i + 2].equals("C")) {
                                temp = lineList[i + 1].replace("\"", "");
                            }

                            //Puntuaren kadentzia lortu
                            if (lineList[i].equals("cadence") && lineList[i + 2].equals("rpm")) {
                                cad = lineList[i + 1].replace("\"", "");
                            }

                            //Puntuaren potentzia lortu
                            if (lineList[i].equals("power") && lineList[i + 2].equals("watts")) {
                                power = lineList[i + 1].replace("\"", "");
                            }
                        }

                        //Koordenatuen eta denboraren informaziorik ez badago ez gorde puntuaren informazioa
                        if (!lat.isBlank() && !lon.isBlank() && !Double.isNaN(Double.parseDouble(lat)) &&
                                !Double.isNaN(Double.parseDouble(lon)) && !time.isBlank()) {

                            //Koordenatuak eta data gorde
                            double latDouble = Double.parseDouble(lat);
                            BigDecimal bdLat = BigDecimal.valueOf(latDouble);
                            bdLat = bdLat.setScale(8, RoundingMode.HALF_UP);

                            double lonDouble = Double.parseDouble(lon);
                            BigDecimal bdLon = BigDecimal.valueOf(lonDouble);
                            bdLon = bdLon.setScale(8, RoundingMode.HALF_UP);

                            coordZerr.add(new Double[]{bdLat.doubleValue(), bdLon.doubleValue()});
                            timeZerr.add(time);

                            //Puntuaren elebazioa gorde
                            try {
                                double eleDouble = Double.parseDouble(ele);
                                eleZerr.add(eleDouble);
                            } catch (NumberFormatException e) {
                                eleZerr.add(null);
                            }

                            //Puntuaren bihotz-maiztasuna gorde
                            try {
                                int hrInt = Integer.parseInt(hr);
                                hrZerr.add(hrInt);
                            } catch (NumberFormatException e) {
                                hrZerr.add(null);
                            }

                            //Puntuaren tenperatura gorde
                            try {
                                double tempDouble = Double.parseDouble(temp);
                                tempZerr.add(tempDouble);
                            } catch (NumberFormatException e) {
                                tempZerr.add(null);
                            }

                            //Puntuaren kadentzia gorde
                            try {
                                int cadInt = Integer.parseInt(cad);
                                cadZerr.add(cadInt);
                            } catch (NumberFormatException e) {
                                cadZerr.add(null);
                            }

                            //Puntuaren potentzia gorde
                            try {
                                int powerInt = Integer.parseInt(power);
                                pwZerr.add(powerInt);
                            } catch (NumberFormatException e) {
                                pwZerr.add(null);
                            }
                        }
                    }

                    line = reader.readLine();
                }

                if (fitxMota == 4 && !coordZerr.stream().allMatch(Objects::isNull) && !timeZerr.stream().allMatch(Objects::isNull)) {
                    if (coordZerr.size() > 1 && coordZerr.size() == timeZerr.size()) {

                        //Jarduera batean informazio mota baten baliorik ez bada aurkitzen zerrenda "null" bezala gorde
                        if (eleZerr.stream().allMatch(Objects::isNull)) eleZerr = null;
                        if (hrZerr.stream().allMatch(Objects::isNull)) hrZerr = null;
                        if (tempZerr.stream().allMatch(Objects::isNull)) tempZerr = null;
                        if (cadZerr.stream().allMatch(Objects::isNull)) cadZerr = null;
                        if (pwZerr.stream().allMatch(Objects::isNull)) pwZerr = null;

                        if (jardMota.equals("Txirrindularitza")) {
                            return new TxirrJardModel(jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                        } else if (jardMota.equals("Korrika")) {
                            return new KorrJardModel(jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                        } else if (jardMota.equals("Ibilaritza")) {
                            return new IbilJardModel(jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                        } else {
                            return new JardueraModel(jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException ignored) {}

        return null;
    }

    private String kalkulatuData(String pTime) {
        try {
            long timeLong = Long.parseLong(pTime);
            Date date = new Date(timeLong);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return sdf.format(date);
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
