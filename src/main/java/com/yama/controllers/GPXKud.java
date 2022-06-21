package com.yama.controllers;

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

    public ArrayList<JSONObject> kudeatuGPX(File fitx) {
        ArrayList<JSONObject> jarduerak = new ArrayList<>();

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
                    JSONObject jardueraJSON = kudeatuTrack((Element) track);
                    jarduerak.add(jardueraJSON);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        return jarduerak;
    }

    private JSONObject kudeatuTrack(Element track) {
        JSONObject jardueraJSON = new JSONObject();

        //Jardueraren izena lortu eta gorde
        String jardIzena = track.getElementsByTagName("name").item(0).getTextContent();
        jardueraJSON.put("name", jardIzena);

        //Jarduera mota lortu eta gorde
        String jardMota = track.getElementsByTagName("type").item(0).getTextContent();
        jardueraJSON.put("type", jardMota);

        JSONArray pointsJSONArray = new JSONArray();

        //Jardueraren segmentuak lortu eta kudeatu
        NodeList segmentuak = track.getElementsByTagName("trkseg");

        //Jardueraren segmentuak aztertu
        for (int i = 0; i < segmentuak.getLength(); i++) {
            Element segmentu = (Element) segmentuak.item(i);

            //Segmentu hutsak filtratzeko
            if (segmentu.getNodeType() == Node.ELEMENT_NODE && segmentu.hasChildNodes()) {
                NodeList wayPoints = segmentu.getElementsByTagName("trkpt");

                //Jardueraren puntuen informazioa lortu eta gorde
                for (int j = 0; j < wayPoints.getLength(); j++) {
                    Element point = (Element) wayPoints.item(i);

                    if (point.getNodeType() == Node.ELEMENT_NODE) {
                        JSONObject pointJSON = new JSONObject();

                        //Puntuaren latitudea eta longitudea lortu
                        String lat = point.getAttribute("lat");
                        String lon = point.getAttribute("lon");

                        //Puntuaren elebazioa lortu
                        String ele = point.getElementsByTagName("ele").item(0).getTextContent();

                        //Puntuaren denbora (data eta ordua) lortu
                        String time = point.getElementsByTagName("time").item(0).getTextContent();

                        //Puntuaren "extensions" nodoa aztertu bestelako atributuak lortzeko
                        Node extensions = point.getElementsByTagName("extensions").item(0);
                        NodeList extsNodoak = extensions.getChildNodes();

                        //Puntuaren bihotz-maiztasuna, tenperatura, kadentzia eta potentzia lortu
                        String hr = getInfo(extsNodoak, new String[]{"hr", "heartrate"});
                        String temp = getInfo(extsNodoak, new String[]{"temp"});
                        String cad = getInfo(extsNodoak, new String[]{"cad"});
                        String power = getInfo(extsNodoak, new String[]{"pow"});

                        //Puntuaren informazioa JSON objektu batean gorde
                        pointJSON.put("latitude", lat);
                        pointJSON.put("longitude", lon);
                        pointJSON.put("elevation", ele);
                        pointJSON.put("time", time);
                        pointJSON.put("heartrate", hr);
                        pointJSON.put("temperature", temp);
                        pointJSON.put("cadence", cad);
                        pointJSON.put("power", power);

                        //Puntuaren informazioa jardueraren puntuen JSON zerrendan gorde
                        pointsJSONArray.put(pointJSON);
                    }
                }
            }
        }

        //Puntuen JSON zerrenda jardueraren JSON objektuan gorde
        jardueraJSON.put("waypoints", pointsJSONArray);

        return jardueraJSON;
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
