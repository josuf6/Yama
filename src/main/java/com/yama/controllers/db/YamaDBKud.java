package com.yama.controllers.db;

import com.yama.models.ErabiltzaileModel;
import javafx.scene.control.TextField;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class YamaDBKud {

    private static final YamaDBKud nYamaDBKud = new YamaDBKud();

    public static YamaDBKud getYamaDBKud() {
        return nYamaDBKud;
    }

    private YamaDBKud() {}

    //TODO aldatu metodoak
    //TODO aldatu metodoak
    //TODO aldatu metodoak
    //TODO aldatu metodoak

    /*public ArrayList<String> lortuUrl() {
        String query = "select url, last_updated from server_historiala";
        ResultSet rs = DBKud.getDBKud().execSQL(query);
        ArrayList<String> urlList = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    String emaitza = rs.getString("url") + " (" + rs.getString("last_updated") + ")";
                    urlList.add(emaitza);
                }
            } catch(SQLException throwables){
                throwables.printStackTrace();
            }
        }
        return urlList;
    }*/

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
}
