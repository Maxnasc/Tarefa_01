package com.example.calculos;

import android.util.Log;

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
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String SECRET_KEY = "D8Xh!^iKFJQng%%Bh59F7N";
    private Key key;
    private SecureRandom sr;
    private byte[] iv;

    private Region dataRegion; // Objeto de região
    private boolean encrypt; // true para criptografar, false para descriptografar

    public CryptoUtils() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // ou 128 ou 192
            this.key = keyGen.generateKey();
            sr = SecureRandom.getInstance("IngrianRNG","IngrianProvider");
            this.iv = new byte[16];
            sr.nextBytes(iv);
        } catch (Exception e) {
            Log.i("Erro na geração da chave", String.valueOf(e));
        }
    }

    public CryptoUtils(int tempo) {
        this.tempo = tempo;
    }

//    @Override
//    public void run() {
//        while(true){
//            try {
//                // Pega a Region do semáforo
//                dataRegion = semaforo.getEncryptRequest();
//                if (dataRegion != null) {
//                    if (dataRegion instanceof Region) {
//                        data
//                    } else if (dataRegion instanceof SubRegion) {
//
//                    } else if (dataRegion instanceof RestrictedRegion) {
//
//                    }
//                }
//                // Pausa de 500ms
//                Thread.sleep(tempo);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

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

    private String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByteValue = cipher.doFinal(data.getBytes("utf-8"));
        return Base64.getEncoder().encodeToString(encryptedByteValue);
    }

    private String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedByteValue = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedByteValue, "utf-8");
    }

    public Map<String, String> encryptRegion(Region region) {
        // Função que monta o objeto de Region encriptado
        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("nome", encrypt(region.getNome()));
            dados.put("posixLatitude", encryptDouble(region.getPosixLatitude()));
            dados.put("posixLongitude", encryptDouble(region.getPosixLongitude()));
            dados.put("user", encryptInt(region.getUser()));
            dados.put("timestamp", encryptLong(region.getTimestamp()));
            return dados;
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }

    public Map<String, String> encryptSubRegion(SubRegion region) {
        // Função que monta o objeto de Region encriptado
        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("mainRegion", encrypt(region.getMainRegion().getNome()));
            dados.put("nome", encrypt(region.getNome()));
            dados.put("posixLatitude", encryptDouble(region.getPosixLatitude()));
            dados.put("posixLongitude", encryptDouble(region.getPosixLongitude()));
            dados.put("user", encryptInt(region.getUser()));
            dados.put("timestamp", encryptLong(region.getTimestamp()));
            return dados;
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }

    public Map<String, String> encryptSRestrictedRegion(RestrictedRegion region) {
        // Função que monta o objeto de Region encriptado
        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("mainRegion", encrypt(region.getMainRegion().getNome()));
            dados.put("nome", encrypt(region.getNome()));
            dados.put("posixLatitude", encryptDouble(region.getPosixLatitude()));
            dados.put("posixLongitude", encryptDouble(region.getPosixLongitude()));
            dados.put("user", encryptInt(region.getUser()));
            dados.put("timestamp", encryptLong(region.getTimestamp()));
            dados.put("restricted", encryptBoolean(region.getRestricted()));
            return dados;
        } catch (Exception e) {
            Log.i("Erro na montagem do objeto", String.valueOf(e));
        }
        return null;
    }
    
    public Region decryptRegion(Map <String, Object> data) {
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

    public SubRegion decryptSubRegion(Map <String, Object> data) {
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

    public RestrictedRegion decryptRestrictedRegion(Map <String, Object> data) {
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
