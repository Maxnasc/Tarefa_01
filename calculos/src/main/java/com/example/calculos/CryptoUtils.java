package com.example.calculos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtils {
    private static final String SECRET_KEY = "chaveSecreta";
    private static final String INIT_VECTOR = "vetorInicial";

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String encryptRegionToJson(Region region) {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(region);
        return encrypt(json);
    }

    public static Region decryptJsonToRegion(String encryptedJson) {
        String decryptedJson = decrypt(encryptedJson);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(decryptedJson, Region.class);
    }

    public static String encryptSubRegionToJson(SubRegion subRegion) {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(subRegion);
        return encrypt(json);
    }

    public static SubRegion decryptJsonToSubRegion(String encryptedJson) {
        String decryptedJson = decrypt(encryptedJson);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(decryptedJson, SubRegion.class);
    }

    public static String encryptRestrictedRegionToJson(RestrictedRegion subRegion) {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(subRegion);
        return encrypt(json);
    }

    public static RestrictedRegion decryptJsonToRestrictedRegion(String encryptedJson) {
        String decryptedJson = decrypt(encryptedJson);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(decryptedJson, RestrictedRegion.class);
    }
}
