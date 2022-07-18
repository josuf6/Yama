package com.yama.controllers.db;

import com.yama.models.ErabiltzaileModel;
import com.yama.models.JardueraModel;
import com.yama.models.TxirrJardModel;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
        ErabiltzaileModel erabiltzailea = null;
        if (rs != null) {
            try {
                rs.next();
                erabiltzailea = new ErabiltzaileModel(rs.getString("ezizena"), rs.getString("izena"), rs.getString("abizena"));
            } catch(SQLException throwables){
                throwables.printStackTrace();
            }
        }
        return erabiltzailea;
    }

    public boolean pasahitzBerdinaDa(String pEzizena, String pPasahitza) {
        String pasBerriHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pPasahitza);
        String query = "select pasahitza from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};
        ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
        if (rs != null) {
            try {
                rs.next();
                String pasahitzZaharHash = rs.getString("pasahitza");
                if (!pasBerriHash.equals(pasahitzZaharHash)) {
                    return false;
                }
            } catch(SQLException throwables){
                throwables.printStackTrace();
            }
        }
        return true;
    }

    public boolean existitzenDaErabiltzailea(String pEzizena) {
        String query = "select * from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};
        ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
        try {
            return rs.next();
        } catch(SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public void erregistratuErabiltzailea(String pEzizena, String pPasahitza, String pIzena, String pAbizena) {
        String pasHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pPasahitza);
        String query = "insert into Erabiltzailea values(?, ?, ?, ?)";
        Object[] datuak = {pEzizena, pasHash, pIzena, pAbizena};
        DBKud.getDBKud().execSQL(query, datuak);
    }

    public void ezabatuErabiltzailea(String pEzizena) {
        String query = "delete from Erabiltzailea where ezizena=?";
        Object[] datuak = {pEzizena};
        DBKud.getDBKud().execSQL(query, datuak);
    }

    public boolean egiaztatuErabiltzailea(String pEzizena, String pPasahitza) {
        String pasHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pPasahitza);
        String query = "select ezizena from Erabiltzailea where ezizena=? and pasahitza=?";
        Object[] datuak = {pEzizena, pasHash};
        ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
        try {
            return rs.next();
        } catch(SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public void eguneratuErabIzen(String ezizena, String izenBerri) {
        String query = "update Erabiltzailea set izena=? where ezizena=?";
        Object[] datuak = {izenBerri, ezizena};
        DBKud.getDBKud().execSQL(query, datuak);
    }

    public void eguneratuErabAbizen(String ezizena, String abiBerri) {
        String query = "update Erabiltzailea set abizena=? where ezizena=?";
        Object[] datuak = {abiBerri, ezizena};
        DBKud.getDBKud().execSQL(query, datuak);
    }

    public void eguneratuErabPasahitz(String ezizena, String pasahitzBerri) {
        String pasHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pasahitzBerri);
        String query = "update Erabiltzailea set pasahitza=? where ezizena=?";
        Object[] datuak = {pasHash, ezizena};
        DBKud.getDBKud().execSQL(query, datuak);
    }

    public void eguneratuErabEzizen(String ezizena, String ezizenBerri) {
        String query = "update Erabiltzailea set ezizena=? where ezizena=?";
        Object[] datuak = {ezizenBerri, ezizena};
        DBKud.getDBKud().execSQL(query, datuak);
    }

    public boolean jardueraGordeta(String pEzizena, JardueraModel pJarduera) {
        String query = "select datuak from Jarduera where ezizena=?";
        Object[] datuak = {pEzizena};
        ResultSet rs = DBKud.getDBKud().execSQL(query, datuak);
        String jardueraDatuak = "";
        if (rs != null) {
            try {
                while (rs.next()) {
                    jardueraDatuak = rs.getString("datuak");
                    if (berdinakDiraJarduerak(formateatuDatuak(pJarduera), jardueraDatuak)) {
                        return true;
                    }
                }
                return false;
            } catch(SQLException throwables){
                throwables.printStackTrace();
            }
        }
        return true;
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
        if (!berdinakDiraZerrendak("dataZerr", jard1DatuakJSON, jard2DatuakJSON)) return false;
        if (!berdinakDiraZerrendak("altZerr", jard1DatuakJSON, jard2DatuakJSON)) return false;
        if (!berdinakDiraZerrendak("bihotzMaizZerr", jard1DatuakJSON, jard2DatuakJSON)) return false;
        if (!berdinakDiraZerrendak("kadZerr", jard1DatuakJSON, jard2DatuakJSON)) return false;
        if (!berdinakDiraZerrendak("potZerr", jard1DatuakJSON, jard2DatuakJSON)) return false;
        if (!berdinakDiraZerrendak("tenpZerr", jard1DatuakJSON, jard2DatuakJSON)) return false;

        return true;
    }


    private boolean berdinakDiraZerrendak(String pZerrIzen, JSONObject jard1DatuakJSON, JSONObject jard2DatuakJSON) {
        JSONArray zerr1 = jard1DatuakJSON.getJSONArray(pZerrIzen);
        JSONArray zerr2 = jard2DatuakJSON.getJSONArray(pZerrIzen);

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

    public void gordeJarduera(String pEzizena, JardueraModel pJarduera) {
        String query = "insert into Jarduera (datuak, ezizena) values(?, ?)";
        String jardDatuak = formateatuDatuak(pJarduera);
        Object[] datuak = {jardDatuak, pEzizena};
        DBKud.getDBKud().execSQL(query, datuak);
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
}
