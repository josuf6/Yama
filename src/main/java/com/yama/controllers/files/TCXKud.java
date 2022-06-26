package com.yama.controllers.files;

import com.yama.models.JardueraModel;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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

        //Jardueraren lap-ak lortu eta kudeatu
        NodeList laps = activity.getElementsByTagName("Lap");

        String jardHasiData = null;
        String jardBukData = null;

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
                                time = timeNodes.item(0).getTextContent();
                            }

                            //Koordenatuen eta denboraren ez badago ez gorde puntuaren informazioa
                            if (!lat.isBlank() && !lon.isBlank() && !time.isBlank()) {
                                coordZerr.add(new Double[]{Double.valueOf(lat), Double.valueOf(lon)});
                                timeZerr.add(time);

                                //Jardueraren data aurkitzen den lehenengo data izango da
                                if (jardHasiData == null) {
                                    jardHasiData = time;
                                }

                                //Jardueraren bukaera data Puntu bakoitzarekin eguneratu
                                jardBukData = time;

                                //Puntuaren elebazioa lortu
                                NodeList eleNodes = point.getElementsByTagName("AltitudeMeters");
                                if (eleNodes.getLength() > 0) {
                                    ele = eleNodes.item(0).getTextContent();
                                    eleZerr.add(Double.valueOf(ele));
                                } else {
                                    eleZerr.add(null);
                                }

                                //Puntuaren bihotz-maiztasuna lortu (aurkitzen ez bada "Extensions" nodoan bilatzen da)
                                Element hrElem = ((Element) point.getElementsByTagName("HeartRateBpm").item(0));
                                if (hrElem != null) {
                                    NodeList hrNodes = hrElem.getElementsByTagName("Value");
                                    if (hrNodes.getLength() > 0) {
                                        hr = hrNodes.item(0).getTextContent();
                                        hrZerr.add(Integer.valueOf(hr));
                                    }
                                }

                                //Puntuaren kadentzia lortu (aurkitzen ez bada "Extensions" nodoan bilatzen da)
                                NodeList cadElem = point.getElementsByTagName("Cadence");
                                if (cadElem.getLength() > 0) {
                                    cad = cadElem.item(0).getTextContent();
                                    cadZerr.add(Integer.valueOf(cad));
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
                                                hrZerr.add(Integer.valueOf(hr));
                                            } else {
                                                hrZerr.add(null);
                                            }
                                        }

                                        temp = getInfo(extsNodoak, new String[]{"temp"});
                                        if (temp != null) {
                                            tempZerr.add(Double.valueOf(temp));
                                        } else {
                                            tempZerr.add(null);
                                        }

                                        if (cad == null) { //lehenik aurkitu ez bada
                                            cad = getInfo(extsNodoak, new String[]{"cad"});
                                            if (cad != null) {
                                                cadZerr.add(Integer.valueOf(cad));
                                            } else {
                                                cadZerr.add(null);
                                            }
                                        }

                                        power = getInfo(extsNodoak, new String[]{"watts", "Watts"});
                                        if (power != null) {
                                            pwZerr.add(Integer.valueOf(power));
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

                return new JardueraModel(jardIzena, jardMota, jardHasiData, jardBukData, coordZerr, eleZerr, timeZerr, hrZerr, tempZerr, cadZerr, pwZerr);
            }
        }
        return null;
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
