package com.yama.controllers.db;

import com.yama.models.*;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Objects;

public class YamaDBKud {

    private static final YamaDBKud nYamaDBKud = new YamaDBKud();

    public static YamaDBKud getYamaDBKud() {
        return nYamaDBKud;
    }

    private YamaDBKud() {}

    public ErabiltzaileModel getErabiltzailea(String pEzizena) {
        String query = "select ezizena, izena, abizena from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};

        ErabiltzaileModel erabiltzailea = null;
        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
            if (rs != null) {
                rs.next();
                erabiltzailea = new ErabiltzaileModel(rs.getString("ezizena"), rs.getString("izena"), rs.getString("abizena"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return erabiltzailea;
    }

    public int pasahitzBerdinaDa(String pEzizena, String pPasahitza) {
        String pasBerriHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pPasahitza);
        String query = "select pasahitza from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};

        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
            if (rs != null) {
                rs.next();
                String pasahitzZaharHash = rs.getString("pasahitza");
                if (!pasBerriHash.equals(pasahitzZaharHash)) return 0;
                else return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int existitzenDaErabiltzailea(String pEzizena) {
        String query = "select * from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};

        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
            if (rs.next()) return 1;
            else return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean erregistratuErabiltzailea(String pEzizena, String pPasahitza, String pIzena, String pAbizena) {
        String pasHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pPasahitza);
        String query = "insert into Erabiltzailea values(?, ?, ?, ?)";
        Object[] datuak = {pEzizena, pasHash, pIzena, pAbizena};
        try {
            DBKud.getDBKud().execSQL(query, datuak);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean ezabatuErabiltzailea(String pEzizena) {
        String query = "delete from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};
        try {
            if (ezabatuErabJarduerak(pEzizena)) {
                DBKud.getDBKud().execSQL(query, datuak);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean ezabatuErabJarduerak(String pEzizena) {
        String query = "delete from Jarduera where ezizena=?";
        Object[] datuak = {pEzizena};
        try {
            DBKud.getDBKud().execSQL(query, datuak);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean ezabatuJarduera(int pId) {
        String query = "delete from Jarduera where id=?";
        Object[] datuak = {pId};
        try {
            DBKud.getDBKud().execSQL(query, datuak);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int egiaztatuErabiltzailea(String pEzizena, String pPasahitza) {
        String pasHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pPasahitza);
        String query = "select ezizena from Erabiltzailea where ezizena=? and pasahitza=?";
        Object[] datuak = {pEzizena, pasHash};
        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
            if (rs.next()) return 1;
            else return 0;
        } catch(SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }

    public boolean eguneratuErabiltzailea(String ezizena, String izenBerri, String abiBerri, String ezizenBerri, String pasahitzBerri) {
        String query;
        Object[] datuak;
        if (!pasahitzBerri.isBlank()) {
            String pasHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pasahitzBerri);
            query = "update Erabiltzailea set ezizena=?, pasahitza=?, izena=?, abizena=? where ezizena=?";
            datuak = new Object[]{ezizenBerri, pasHash, izenBerri, abiBerri, ezizena};
        } else {
            query = "update Erabiltzailea set ezizena=?, izena=?, abizena=? where ezizena=?";
            datuak = new Object[]{ezizenBerri, izenBerri, abiBerri, ezizena};
        }

        try {
            DBKud.getDBKud().execSQL(query, datuak);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int jardueraGordeta(String pEzizena, JardueraModel pJarduera) {
        String query = "select datuak from Jarduera where ezizena=?";
        Object[] datuak = {pEzizena};
        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
            String jardueraDatuak = "";
            if (rs != null) {
                while (rs.next()) {
                    jardueraDatuak = rs.getString("datuak");
                    if (berdinakDiraJarduerak(formateatuDatuak(pJarduera), jardueraDatuak)) {
                        return 1;
                    }
                }
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean berdinakDiraJarduerak(String pJard1Datuak, String pJard2Datuak) {
        JSONObject jard1DatuakJSON = new JSONObject(pJard1Datuak);
        JSONObject jard2DatuakJSON = new JSONObject(pJard2Datuak);

        //Koordenatuen zerrendak aztertu
        JSONArray koordZerr1 = jard1DatuakJSON.getJSONArray("koordZerr");
        JSONArray koordZerr2 = jard2DatuakJSON.getJSONArray("koordZerr");
        if (koordZerr1.length() != koordZerr2.length()) {
            return false;
        } else {
            for (int i = 0; i < koordZerr1.length(); i++) {
                double lat1 = koordZerr1.getJSONArray(i).getDouble(0);
                double lon1 = koordZerr1.getJSONArray(i).getDouble(1);
                double lat2 = koordZerr1.getJSONArray(i).getDouble(0);
                double lon2 = koordZerr1.getJSONArray(i).getDouble(1);
                if (lat1 != lat2 || lon1 != lon2) {
                    return false;
                }
            }
        }

        //Beste zerrendak aztertu
        if (!berdinakDiraZerrendak(jard1DatuakJSON.getJSONArray("dataZerr"), jard2DatuakJSON.getJSONArray("dataZerr"))) return false;
        if (!berdinakDiraZerrendak(jard1DatuakJSON.getJSONArray("altZerr"), jard2DatuakJSON.getJSONArray("altZerr"))) return false;
        if (!berdinakDiraZerrendak(jard1DatuakJSON.getJSONArray("bihotzMaizZerr"), jard2DatuakJSON.getJSONArray("bihotzMaizZerr"))) return false;
        if (!berdinakDiraZerrendak(jard1DatuakJSON.getJSONArray("kadZerr"), jard2DatuakJSON.getJSONArray("kadZerr"))) return false;
        if (!berdinakDiraZerrendak(jard1DatuakJSON.getJSONArray("potZerr"), jard2DatuakJSON.getJSONArray("potZerr"))) return false;
        if (!berdinakDiraZerrendak(jard1DatuakJSON.getJSONArray("tenpZerr"), jard2DatuakJSON.getJSONArray("tenpZerr"))) return false;

        return true;
    }


    private boolean berdinakDiraZerrendak(JSONArray zerr1, JSONArray zerr2) {
        if (zerr1.length() != zerr2.length()) {
            return false;
        } else {
            for (int i = 0; i < zerr1.length(); i++) {
                Object obj1 = zerr1.get(i);
                Object obj2 = zerr2.get(i);

                if (!Objects.equals(obj1, obj2)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean gordeJarduera(String pEzizena, JardueraModel pJarduera) {
        String query = "insert into Jarduera (datuak, ezizena) values(?, ?)";
        String jardDatuak = formateatuDatuak(pJarduera);
        Object[] datuak = {jardDatuak, pEzizena};
        try {
            DBKud.getDBKud().execSQL(query, datuak);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getAzkenJardId() {
        String query = "select id from Jarduera order by id desc";
        Object[] datuaBerriak = {};
        int emaitza = -1;
        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuaBerriak);
            if (rs != null) {
                rs.next();
                emaitza = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emaitza;
    }

    public ArrayList<JardueraModel> getErabJarduerak(String pEzizena) {
        String query = "select * from Jarduera where ezizena=?";
        Object[] datuak = {pEzizena};
        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
            if (rs != null) {
                ArrayList<JardueraModel> jardZerr = new ArrayList<>();
                while (rs.next()) {
                    jardZerr.add(formateatuJarduera(rs.getInt("id"), rs.getString("datuak")));
                }
                return jardZerr;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JardueraModel getJarduera(int idDB) {
        String query = "select id, datuak from Jarduera where id=?";
        Object[] datuaBerriak = {idDB};
        try {
            ResultSet rs = DBKud.getDBKud().execSQL(query, datuaBerriak);
            if (rs != null) {
                rs.next();
                String datuak = rs.getString("datuak");
                return formateatuJarduera(idDB, datuak);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean eguneratuJarduera(int idDB, JardueraModel pJarduera) {
        String query = "update Jarduera set datuak=? where id=?";
        String jardDatuak = formateatuDatuak(pJarduera);
        Object[] datuak = {jardDatuak, idDB};
        try {
            DBKud.getDBKud().execSQL(query, datuak);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String formateatuDatuak(JardueraModel pJarduera) {
        JSONObject jardDatuakJSON = new JSONObject();
        jardDatuakJSON.put("izena", pJarduera.getIzenaBal());
        jardDatuakJSON.put("mota", pJarduera.getMotaBal());
        jardDatuakJSON.put("koordZerr", pJarduera.getKoordZerr());
        jardDatuakJSON.put("dataZerr", pJarduera.getDataZerr());
        jardDatuakJSON.put("altZerr", pJarduera.getAltZerr());
        jardDatuakJSON.put("bihotzMaizZerr", pJarduera.getBihotzMaizZerr());
        jardDatuakJSON.put("kadZerr", pJarduera.getKadZerr());
        jardDatuakJSON.put("potZerr", pJarduera.getPotZerr());
        jardDatuakJSON.put("tenpZerr", pJarduera.getTenpZerr());
        return String.valueOf(jardDatuakJSON);
    }

    private JardueraModel formateatuJarduera(int idDB, String datuak) {
        JSONObject jardDatuakJSON = new JSONObject(datuak);
        String izena = jardDatuakJSON.getString("izena");
        String mota = jardDatuakJSON.getString("mota");

        //Koordenatuen zerrenda osatu
        ArrayList<Double[]> koordZerr = new ArrayList<>();
        JSONArray koordZerrJSON = jardDatuakJSON.getJSONArray("koordZerr");
        if (koordZerrJSON.length() > 0) {
            for (int i = 0; i < koordZerrJSON.length(); i++) {
                double lat = koordZerrJSON.getJSONArray(i).getDouble(0);
                double lon = koordZerrJSON.getJSONArray(i).getDouble(1);
                koordZerr.add(new Double[]{lat, lon});
            }
        } else {
            koordZerr = null;
        }

        //Beste zerrendak osatu
        ArrayList<String> dataZerr = zerrendaOsatuString(jardDatuakJSON.getJSONArray("dataZerr"));
        ArrayList<Double> altZerr = zerrendaOsatuDouble(jardDatuakJSON.getJSONArray("altZerr"));
        ArrayList<Integer> bihotzMaizZerr = zerrendaOsatuInteger(jardDatuakJSON.getJSONArray("bihotzMaizZerr"));
        ArrayList<Integer> kadZerr = zerrendaOsatuInteger(jardDatuakJSON.getJSONArray("kadZerr"));
        ArrayList<Integer> potZerr = zerrendaOsatuInteger(jardDatuakJSON.getJSONArray("potZerr"));
        ArrayList<Double> tenpZerr = zerrendaOsatuDouble(jardDatuakJSON.getJSONArray("tenpZerr"));

        //Sortutako objektua itzuli
        if (mota.equals("Txirrindularitza")) {
            return new TxirrJardModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
        } else if (mota.equals("Korrika")) {
            return new KorrJardModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
        } else if (mota.equals("Ibilaritza")) {
            return new IbilJardModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
        } else {
            return new JardueraModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
        }
    }

    private ArrayList<String> zerrendaOsatuString(JSONArray zerrJSON) {
        ArrayList<String> zerrArray = new ArrayList<>();
        if (zerrJSON.length() > 0) {
            for (int i = 0; i < zerrJSON.length(); i++) {
                if (zerrJSON.get(i) != null) zerrArray.add(zerrJSON.getString(i));
                else zerrArray.add(null);
            }
            return zerrArray;
        }
        return null;
    }

    private ArrayList<Integer> zerrendaOsatuInteger(JSONArray zerrJSON) {
        ArrayList<Integer> zerrArray = new ArrayList<>();
        if (zerrJSON.length() > 0) {
            for (int i = 0; i < zerrJSON.length(); i++) {
                if (!zerrJSON.get(i).equals(null)) zerrArray.add(zerrJSON.getInt(i));
                else zerrArray.add(null);
            }
            return zerrArray;
        }
        return null;
    }

    private ArrayList<Double> zerrendaOsatuDouble(JSONArray zerrJSON) {
        ArrayList<Double> zerrArray = new ArrayList<>();
        if (zerrJSON.length() > 0) {
            for (int i = 0; i < zerrJSON.length(); i++) {
                if (zerrJSON.get(i) != null) zerrArray.add(zerrJSON.getDouble(i));
                else zerrArray.add(null);
            }
            return zerrArray;
        }
        return null;
    }
}
