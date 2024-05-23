package com.example.calculos;

import android.util.Log;

import com.google.gson.JsonObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class CryptoUtils {
    private int tempo; // Período da thread
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345";

    private Region dataRegion; // Objeto de região
    private boolean encrypt; // true para criptografar, false para descriptografar

    public CryptoUtils() {
    }

    private String encryptDouble(double data) throws Exception {
        return encrypt(String.valueOf(data));
    }

    private double decryptDouble(String encryptedData) throws Exception {
        return Double.parseDouble(decrypt(encryptedData));
    }

    private String encryptInt(int data) throws Exception {
        return encrypt(String.valueOf(data));
    }

    private int decryptInt(String encryptedData) throws Exception {
        return Integer.parseInt(decrypt(encryptedData));
    }

    private String encryptLong(long data) throws Exception {
        return encrypt(String.valueOf(data));
    }

    private long decryptLong(String encryptedData) throws Exception {
        return Long.parseLong(decrypt(encryptedData));
    }

    private String encryptBoolean(boolean data) throws Exception {
        return encrypt(String.valueOf(data));
    }

    private boolean decryptBoolean(String encryptedData) throws Exception {
        return Boolean.parseBoolean(decrypt(encryptedData));
    }

    private static Key generateKey(String secretKey) {
        return new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
    }

    private String encrypt(String data) throws Exception {
        try {
            Key key = generateKey(SECRET_KEY);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }

    private String decrypt(String encryptedData) throws Exception {
        try {
            Key key = generateKey(SECRET_KEY);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }

    public JsonObject encryptRegion(Region region) {
        // Função que monta o objeto de Region encriptado
        try {
            JsonObject dados = new JsonObject();
            dados.addProperty("nome", encrypt(region.getNome()));
            dados.addProperty("posixLatitude", encryptDouble(region.getPosixLatitude()));
            dados.addProperty("posixLongitude", encryptDouble(region.getPosixLongitude()));
            dados.addProperty("user", encryptInt(region.getUser()));
            dados.addProperty("timestamp", encryptLong(region.getTimestamp()));
            return dados;
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }

    public JsonObject encryptSubRegion(SubRegion region) {
        // Função que monta o objeto de Region encriptado
        try {
            JsonObject dados = new JsonObject();
            dados.addProperty("mainRegion", encrypt(region.getMainRegion().getNome()));
            dados.addProperty("nome", encrypt(region.getNome()));
            dados.addProperty("posixLatitude", encryptDouble(region.getPosixLatitude()));
            dados.addProperty("posixLongitude", encryptDouble(region.getPosixLongitude()));
            dados.addProperty("user", encryptInt(region.getUser()));
            dados.addProperty("timestamp", encryptLong(region.getTimestamp()));
            return dados;
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }

    public JsonObject encryptSRestrictedRegion(RestrictedRegion region) {
        // Função que monta o objeto de Region encriptado
        try {
            JsonObject dados = new JsonObject();
            dados.addProperty("mainRegion", encrypt(region.getMainRegion().getNome()));
            dados.addProperty("nome", encrypt(region.getNome()));
            dados.addProperty("posixLatitude", encryptDouble(region.getPosixLatitude()));
            dados.addProperty("posixLongitude", encryptDouble(region.getPosixLongitude()));
            dados.addProperty("user", encryptInt(region.getUser()));
            dados.addProperty("timestamp", encryptLong(region.getTimestamp()));
            dados.addProperty("restricted", encryptBoolean(region.getRestricted()));
            return dados;
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }
    
    public Region decryptRegion(Map<String, Object> data) {
        Region objToSend = new Region();
        try {
            objToSend.setNome(decrypt((String) data.get("nome")));
            objToSend.setPosixLatitude(decryptDouble((String) data.get("posixLatitude")));
            objToSend.setPosixLongitude(decryptDouble((String) data.get("posixLongitude")));
            objToSend.setUser(decryptInt((String) data.get("user")));
            objToSend.setTimestamp(decryptLong((String) data.get("timestamp")));
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return objToSend;
    }

    public SubRegion decryptSubRegion(Map<String, Object> data) {
        SubRegion objToSend = new SubRegion();
        Region mainRegion = new Region();
        try {
            mainRegion.setNome(decrypt((String) data.get("mainRegion"))); // Montando objeto com mainRegion -> apenas referencia de nome
            // Montagem do objeto
            objToSend.setMainRegion(mainRegion);
            objToSend.setNome(decrypt((String) data.get("nome")));
            objToSend.setPosixLatitude(decryptDouble((String) data.get("posixLatitude")));
            objToSend.setPosixLongitude(decryptDouble((String) data.get("posixLongitude")));
            objToSend.setUser(decryptInt((String) data.get("user")));
            objToSend.setTimestamp(decryptLong((String) data.get("timestamp")));
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return objToSend;
    }

    public RestrictedRegion decryptRestrictedRegion(Map<String, Object> data) {
        RestrictedRegion objToSend = new RestrictedRegion();
        Region mainRegion = new Region();
        try {
            mainRegion.setNome(decrypt((String) data.get("mainRegion"))); // Montando objeto com mainRegion -> apenas referencia de nome
            // Montagem do objeto
            objToSend.setMainRegion(mainRegion);
            objToSend.setNome(decrypt((String) data.get("nome")));
            objToSend.setPosixLatitude(decryptDouble((String) data.get("posixLatitude")));
            objToSend.setPosixLongitude(decryptDouble((String) data.get("posixLongitude")));
            objToSend.setUser(decryptInt((String) data.get("user")));
            objToSend.setTimestamp(decryptLong((String) data.get("timestamp")));
            objToSend.setRestricted(decryptBoolean((String) data.get("restricted")));
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return objToSend;
    }

}
