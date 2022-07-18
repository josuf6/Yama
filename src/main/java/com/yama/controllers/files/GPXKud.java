package com.yama.controllers.files;

import com.yama.models.IbilJardModel;
import com.yama.models.JardueraModel;
import com.yama.models.KorrJardModel;
import com.yama.models.TxirrJardModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GPXKud {

    private static final GPXKud nGPXKud = new GPXKud();

    public static GPXKud getGPXKud() {
        return nGPXKud;
    }

    private GPXKud() {}

    public ArrayList<JardueraModel> kudeatuGPX(File fitx) {
        ArrayList<JardueraModel> jarduerak = new ArrayList<>();

        try {
            File fitxGPX = new File(fitx.getAbsolutePath());
            DocumentBuilderFactory dbFact =  DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuild = dbFact.newDocumentBuilder();
            Document gpx = dBuild.parse(fitxGPX);

            NodeList tracks = gpx.getElementsByTagName("trk");

            //GPX fitxategi batean Track bat baino gehiago egon daiteke
            //Track horietako bakoitza jarduera bat bezala tratatu
            for (int i = 0; i < tracks.getLength(); i++) { //Nodo baten semeak aztertu
                Node track = tracks.item(i);

                //Track hutsak filtratzeko
                if (track.getNodeType() == Node.ELEMENT_NODE && track.hasChildNodes()) {
                    JardueraModel jarduera = kudeatuTrack((Element) track);

                    if (jarduera != null) {
                        jarduerak.add(jarduera);
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return jarduerak;
    }

    private JardueraModel kudeatuTrack(Element track) {

        //Jardueraren izena lortu eta gorde
        String jardIzena = "";
        NodeList izenaNodes = track.getElementsByTagName("name");
        if (izenaNodes.getLength() > 0) {
            jardIzena = izenaNodes.item(0).getTextContent();
        }

        //Jarduera mota lortu eta gorde
        String jardMota = "";
        NodeList motaNodes = track.getElementsByTagName("type");
        if (motaNodes.getLength() > 0) {
            jardMota = motaNodes.item(0).getTextContent().toLowerCase();

            if (Arrays.stream(new String[]{"bik", "cycl"}).anyMatch(jardMota::contains)) {
                jardMota =  "Txirrindularitza";
            } else if (Arrays.stream(new String[]{"run"}).anyMatch(jardMota::contains)) {
                jardMota =  "Korrika";
            } else if (Arrays.stream(new String[]{"walk", "hik"}).anyMatch(jardMota::contains)) {
                jardMota =  "Ibilaritza";
            } else {
                jardMota = "";
            }
        }

        //Jardueraren segmentuak lortu eta kudeatu
        NodeList segmentuak = track.getElementsByTagName("trkseg");

        //Jardueraren puntu bakoitzean aztertuko diren informaziorako zerrendak
        ArrayList<Double[]> coordZerr = new ArrayList<>();
        ArrayList<Double> eleZerr = new ArrayList<>();
        ArrayList<String> timeZerr = new ArrayList<>();
        ArrayList<Integer> hrZerr = new ArrayList<>();
        ArrayList<Double> tempZerr = new ArrayList<>();
        ArrayList<Integer> cadZerr = new ArrayList<>();
        ArrayList<Integer> pwZerr = new ArrayList<>();

        //Jardueraren segmentuak aztertu
        for (int i = 0; i < segmentuak.getLength(); i++) {
            Element segmentu = (Element) segmentuak.item(i);

            //Segmentu hutsak filtratzeko
            if (segmentu.getNodeType() == Node.ELEMENT_NODE && segmentu.hasChildNodes()) {
                NodeList wayPoints = segmentu.getElementsByTagName("trkpt");

                //Jardueraren puntuen informazioa lortu eta gorde
                for (int j = 0; j < wayPoints.getLength(); j++) {
                    Element point = (Element) wayPoints.item(j);

                    if (point.getNodeType() == Node.ELEMENT_NODE) {
                        String lat, lon, ele, hr, temp, cad, power;
                        String time = "";

                        //Puntuaren koordenatuak lortu
                        lat = point.getAttribute("lat");
                        lon = point.getAttribute("lon");

                        //Puntuaren denbora (data eta ordua) lortu
                        NodeList timeNodes = point.getElementsByTagName("time");
                        if (timeNodes.getLength() > 0) {
                            time = getTime(timeNodes.item(0).getTextContent());
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

                            //Puntuaren elebazioa lortu
                            NodeList eleNodes = point.getElementsByTagName("ele");
                            if (eleNodes.getLength() > 0) {
                                try {
                                    ele = eleNodes.item(0).getTextContent();
                                    double eleDouble = Double.parseDouble(ele);
                                    eleZerr.add(eleDouble);
                                } catch (NumberFormatException e) {
                                    eleZerr.add(null);
                                }
                            } else {
                                eleZerr.add(null);
                            }

                            //Puntuaren "extensions" nodoa aztertu bestelako atributuak lortzeko
                            Node extensions = point.getElementsByTagName("extensions").item(0);
                            if (extensions != null) {
                                NodeList extsNodoak = extensions.getChildNodes();

                                //Puntuaren bihotz-maiztasuna, tenperatura, kadentzia eta potentzia lortu (existitzekotan)
                                if (extsNodoak.getLength() > 0) {
                                    hr = getInfo(extsNodoak, new String[]{"hr", "heartrate"});
                                    if (hr != null) {
                                        try {
                                            int hrInt = Integer.parseInt(hr);
                                            hrZerr.add(hrInt);
                                        } catch (NumberFormatException e) {
                                            hrZerr.add(null);
                                        }
                                    } else {
                                        hrZerr.add(null);
                                    }

                                    temp = getInfo(extsNodoak, new String[]{"temp"});
                                    if (temp != null) {
                                        try {
                                            double tempDouble = Double.parseDouble(temp);
                                            tempZerr.add(tempDouble);
                                        } catch (NumberFormatException e) {
                                            tempZerr.add(null);
                                        }
                                    } else {
                                        tempZerr.add(null);
                                    }

                                    cad = getInfo(extsNodoak, new String[]{"cad"});
                                    if (cad != null) {
                                        try {
                                            int cadInt = Integer.parseInt(cad);
                                            cadZerr.add(cadInt);
                                        } catch (NumberFormatException e) {
                                            cadZerr.add(null);
                                        }
                                    } else {
                                        cadZerr.add(null);
                                    }

                                    power = getInfo(extsNodoak, new String[]{"pw", "pow", "watt"});
                                    if (power != null) {
                                        try {
                                            int powerInt = Integer.parseInt(power);
                                            pwZerr.add(powerInt);
                                        } catch (NumberFormatException e) {
                                            pwZerr.add(null);
                                        }
                                    } else {
                                        pwZerr.add(null);
                                    }
                                }
                            } else {
                                hrZerr.add(null);
                                tempZerr.add(null);
                                cadZerr.add(null);
                                pwZerr.add(null);
                            }
                        }
                    }
                }
            }
        }

        if (!coordZerr.stream().allMatch(Objects::isNull) && !timeZerr.stream().allMatch(Objects::isNull)) {
            if (coordZerr.size() > 1 && coordZerr.size() == timeZerr.size()) {

                //Jarduera batean informazio mota baten baliorik ez bada aurkitzen zerrenda "null" bezala gorde
                if (eleZerr.stream().allMatch(Objects::isNull)) eleZerr = null;
                if (hrZerr.stream().allMatch(Objects::isNull)) hrZerr = null;
                if (tempZerr.stream().allMatch(Objects::isNull)) tempZerr = null;
                if (cadZerr.stream().allMatch(Objects::isNull)) cadZerr = null;
                if (pwZerr.stream().allMatch(Objects::isNull)) pwZerr = null;

                if (jardMota.equals("Txirrindularitza")) {
                    return new TxirrJardModel(-1, jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                } else if (jardMota.equals("Korrika")) {
                    return new KorrJardModel(-1, jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                } else if (jardMota.equals("Ibilaritza")) {
                    return new IbilJardModel(-1, jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                } else {
                    return new JardueraModel(-1, jardIzena, jardMota, coordZerr, eleZerr, timeZerr, hrZerr, cadZerr, pwZerr, tempZerr);
                }
            }
        }
        return null;
    }

    private String getTime(String pTime) {
        String[] dataFormatuak = new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
        for (String formatu : dataFormatuak) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(formatu);
                Date data = sdf.parse(pTime);
                SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                return sdfTime.format(data);
            } catch (ParseException ignored) {}
        }
        return "";
    }

    private String getInfo(NodeList nodoak, String[] pTags) {

        //Errekurtsiboki zeharkatu extensions nodoaren semeak, nodo horien izenak aztertuz, semerik ez duen eta haren
        //izena pTags-en dagoen elementu baten parekoa den nodo bat aurkitu arte. Nodo horren balioa bueltatu
        String balioa = null;
        String balioPosible = null;

        for (int i = 0; i < nodoak.getLength(); i++) { //Nodo baten semeak aztertu
            Node nodo = nodoak.item(i);

            if (!nodo.hasChildNodes()) { //Nodo batek semerik ez badu

                //Nodo baten izena pTags-en dagoen elementu baten parekoa bada
                if (Arrays.stream(pTags).anyMatch(nodo.getParentNode().getNodeName()::contains)) {
                    balioPosible = nodo.getNodeValue();
                }
            } else { //Nodo bat semerik badu, hurak aztertu errekurtsiboki
                balioPosible = getInfo(nodo.getChildNodes(), pTags);
            }

            if (balioPosible != null) { //null ez den balio posible bat aurkitzerakoan balio bezala gorde
                balioa = balioPosible;
            }
        }

        return balioa;
    }
}
