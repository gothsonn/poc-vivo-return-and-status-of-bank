package com.accenture.PocVivoReturnAndStatusOfBank.service;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

@Service
@NoArgsConstructor
public class AES256 {

    @Value("${secret.key}")
    private String SECRET_KEY_USER;

    private String keyFactor = "PBKDF2WithHmacSHA256";

    private String factor = "AES/CBC/PKCS5Padding";

    private Integer iterationCount = 65536;

    private Integer keyLength = 256;

    private static byte[] SALT = {36, -118, 97, 101, -61, 44, 28, 76, 20, 91, -69, 124, -121, -55, -88, -19};

    private static byte[] bytesIV = {36, -118, 97, 101, -61, 44, 28, 76, 20, 91, -69, 124, -121, -55, -88, -19};

    private static final IvParameterSpec ivspec = new IvParameterSpec(bytesIV);

    public String encrypt(String strToEncrypt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(keyFactor);
            KeySpec spec = new PBEKeySpec(SECRET_KEY_USER.toCharArray(), SALT, iterationCount, keyLength);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(factor);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.getMessage());
        }
        return null;
    }

    public String decrypt(String strToDecrypt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(keyFactor);
            KeySpec spec = new PBEKeySpec(SECRET_KEY_USER.toCharArray(), SALT, iterationCount, keyLength);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(factor);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.getMessage());
        }
        return null;
    }
}