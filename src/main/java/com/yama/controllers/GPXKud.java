package com.yama.controllers;

import com.yama.models.JardueraModel;
import org.json.JSONArray;
import org.json.JSONObject;
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
                    jarduerak.add(jarduera);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return jarduerak;
    }

    private JardueraModel kudeatuTrack(Element track) {

        //Jardueraren izena lortu eta gorde
        String jardIzena = track.getElementsByTagName("name").item(0).getTextContent();

        //Jarduera mota lortu eta gorde
        String jardMota = track.getElementsByTagName("type").item(0).getTextContent();

        JSONArray pointsJSONArray = new JSONArray();

        //Jardueraren segmentuak lortu eta kudeatu
        NodeList segmentuak = track.getElementsByTagName("trkseg");

        String jardHasiData = null;
        String jardBukData = null;

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
                        JSONObject pointJSON = new JSONObject();

                        //Puntuaren latitudea eta longitudea lortu
                        String lat = point.getAttribute("lat");
                        String lon = point.getAttribute("lon");
                        if (!lat.isBlank() && !lon.isBlank()) {
                            pointJSON.put("latitude", lat);
                            pointJSON.put("longitude", lon);
                        }

                        //Puntuaren elebazioa lortu
                        NodeList eleNodes = point.getElementsByTagName("ele");
                        if (eleNodes.getLength() > 0) {
                            String ele = eleNodes.item(0).getTextContent();
                            pointJSON.put("elevation", ele);
                        }

                        //Puntuaren denbora (data eta ordua) lortu
                        NodeList timeNodes = point.getElementsByTagName("time");
                        if (timeNodes.getLength() > 0) {
                            String time = timeNodes.item(0).getTextContent();
                            pointJSON.put("time", time);

                            //Jardueraren data aurkitzen den lehenengo data izango da
                            if (jardHasiData == null) {
                                jardHasiData = time;
                            }

                            jardBukData = time;
                        }

                        //Puntuaren "extensions" nodoa aztertu bestelako atributuak lortzeko
                        Node extensions = point.getElementsByTagName("extensions").item(0);
                        if (extensions != null) {
                            NodeList extsNodoak = extensions.getChildNodes();

                            //Puntuaren bihotz-maiztasuna, tenperatura, kadentzia eta potentzia lortu (existitzekotan)
                            if (extsNodoak.getLength() > 0) {
                                String hr = getInfo(extsNodoak, new String[]{"hr", "heartrate"});
                                if (hr != null) {
                                    pointJSON.put("heartrate", hr);
                                }

                                String temp = getInfo(extsNodoak, new String[]{"temp"});
                                if (temp != null) {
                                    pointJSON.put("temperature", temp);
                                }

                                String cad = getInfo(extsNodoak, new String[]{"cad"});
                                if (hr != null) {
                                    pointJSON.put("cadence", cad);
                                }

                                String power = getInfo(extsNodoak, new String[]{"pow", "watt"});
                                if (hr != null) {
                                    pointJSON.put("power", power);
                                }
                            }
                        }

                        //Puntuaren informazioa jardueraren puntuen JSON zerrendan gorde
                        pointsJSONArray.put(pointJSON);
                    }
                }
            }
        }

        return new JardueraModel(jardIzena, jardMota, jardHasiData, jardBukData, pointsJSONArray);
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
