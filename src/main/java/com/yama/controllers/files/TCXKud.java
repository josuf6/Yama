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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TCXKud {

    private static final TCXKud nTCXKud = new TCXKud();

    public static TCXKud getTCXKud() {
        return nTCXKud;
    }

    private TCXKud() {}

    public ArrayList<JardueraModel> kudeatuTCX(File fitx) {
        ArrayList<JardueraModel> jarduerak = new ArrayList<>();

        try {
            File fitxTCX = new File(fitx.getAbsolutePath());
            DocumentBuilderFactory dbFact =  DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuild = dbFact.newDocumentBuilder();
            Document tcx = dBuild.parse(fitxTCX);

            Element activitiesElem = ((Element) tcx.getElementsByTagName("Activities").item(0));
            if (activitiesElem != null) {
                NodeList activities = activitiesElem.getElementsByTagName("Activity");

                //TCX fitxategi batean Activity bat baino gehiago egon daiteke
                //Activity horietako bakoitza jarduera bat bezala tratatu
                for (int i = 0; i < activities.getLength(); i++) { //Nodo baten semeak aztertu
                    Node activity = activities.item(i);

                    //Activity hutsak filtratzeko
                    if (activity.getNodeType() == Node.ELEMENT_NODE && activity.hasChildNodes()) {
                        JardueraModel jarduera = kudeatuTrack((Element) activity);

                        if (jarduera != null) {
                            jarduerak.add(jarduera);
                        }
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return jarduerak;
    }

    private JardueraModel kudeatuTrack(Element activity) {

        //TCX fitxategietan ez da jardueraren izenik gordetzen
        String jardIzena = "";

        //Jarduera mota lortu eta gorde
        String jardMota = activity.getAttribute("Sport").toLowerCase();

        if (Arrays.stream(new String[]{"bik", "cycl"}).anyMatch(jardMota::contains)) {
            jardMota =  "Txirrindularitza";
        } else if (Arrays.stream(new String[]{"run"}).anyMatch(jardMota::contains)) {
            jardMota =  "Korrika";
        } else if (Arrays.stream(new String[]{"walk", "hik"}).anyMatch(jardMota::contains)) {
            jardMota =  "Ibilaritza";
        } else {
            jardMota = "";
        }

        //Jardueraren lap-ak lortu eta kudeatu
        NodeList laps = activity.getElementsByTagName("Lap");

        //Jardueraren puntu bakoitzean aztertuko diren informaziorako zerrendak
        ArrayList<Double[]> coordZerr = new ArrayList<>();
        ArrayList<Double> eleZerr = new ArrayList<>();
        ArrayList<String> timeZerr = new ArrayList<>();
        ArrayList<Integer> hrZerr = new ArrayList<>();
        ArrayList<Double> tempZerr = new ArrayList<>();
        ArrayList<Integer> cadZerr = new ArrayList<>();
        ArrayList<Integer> pwZerr = new ArrayList<>();

        //Jardueraren lap-ak aztertu
        for (int i = 0; i < laps.getLength(); i++) {
            Element lap = (Element) laps.item(i);

            //Lap hutsak filtratzeko
            if (lap.getNodeType() == Node.ELEMENT_NODE && lap.hasChildNodes()) {
                NodeList trackPoints = ((Element) lap.getElementsByTagName("Track").item(0)).getElementsByTagName("Trackpoint");

                //Jardueraren puntuen informazioa lortu eta gorde
                for (int j = 0; j < trackPoints.getLength(); j++) {
                    Element point = (Element) trackPoints.item(j);

                    if (point.getNodeType() == Node.ELEMENT_NODE) {
                        String ele, temp, power;
                        String lat = "", lon = "", time = "";
                        String hr = null, cad = null;

                        //Puntuaren latitudea eta longitudea lortu
                        Element position = (Element) point.getElementsByTagName("Position").item(0);
                        if (position.hasChildNodes()) {

                            //Puntuaren koordenatuak lortu
                            NodeList latNodes = point.getElementsByTagName("LatitudeDegrees");
                            NodeList lonNodes = point.getElementsByTagName("LongitudeDegrees");
                            if (latNodes.getLength() > 0 && lonNodes.getLength() > 0) {
                                lat = latNodes.item(0).getTextContent();
                                lon = lonNodes.item(0).getTextContent();
                            }

                            //Puntuaren denbora (data eta ordua) lortu
                            NodeList timeNodes = point.getElementsByTagName("Time");
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
                                NodeList eleNodes = point.getElementsByTagName("AltitudeMeters");
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

                                //Puntuaren bihotz-maiztasuna lortu (aurkitzen ez bada "Extensions" nodoan bilatzen da)
                                Element hrElem = ((Element) point.getElementsByTagName("HeartRateBpm").item(0));
                                if (hrElem != null) {
                                    NodeList hrNodes = hrElem.getElementsByTagName("Value");
                                    if (hrNodes.getLength() > 0) {
                                        try {
                                            hr = hrNodes.item(0).getTextContent();
                                            int hrInt = Integer.parseInt(hr);
                                            hrZerr.add(hrInt);
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }

                                //Puntuaren kadentzia lortu (aurkitzen ez bada "Extensions" nodoan bilatzen da)
                                NodeList cadElem = point.getElementsByTagName("Cadence");
                                if (cadElem.getLength() > 0) {
                                    try {
                                        cad = cadElem.item(0).getTextContent();
                                        int cadInt = Integer.parseInt(cad);
                                        cadZerr.add(cadInt);
                                    } catch (NumberFormatException ignored) {}
                                }

                                //Puntuaren "Extensions" nodoa aztertu bestelako atributuak lortzeko
                                Node extensions = point.getElementsByTagName("Extensions").item(0);
                                if (extensions != null) {
                                    NodeList extsNodoak = extensions.getChildNodes();

                                    //Puntuaren bihotz-maiztasuna, tenperatura, kadentzia eta potentzia lortu (existitzekotan)
                                    if (extsNodoak.getLength() > 0) {
                                        if (hr == null) { //lehenik aurkitu ez bada
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

                                        if (cad == null) { //lehenik aurkitu ez bada
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
                                        }

                                        power = getInfo(extsNodoak, new String[]{"watts", "Watts"});
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
