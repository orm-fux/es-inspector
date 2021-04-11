package com.github.ormfux.esi.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.github.ormfux.esi.exception.ApplicationException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptionUtils {

    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    
    private static final int TAG_LENGTH_BIT = 128;
    
    private static final int IV_LENGTH_BYTE = 12;
    
    private static final int SALT_LENGTH_BYTE = 16;
    
    private static final SecureRandom SALT_GENERATOR = new SecureRandom();
    
    private static final char[] ENCODING_SECRET = "13056537-0355-4b17-826c-9b445e0a6742".toCharArray();
    
    public static class JsonEncryptSerializer extends JsonSerializer<String> {

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                gen.writeString(encodeText(value));
            }
        }
        
    }
    
    @SuppressWarnings("serial")
    public static class JsonDecryptDeserializer extends StringDeserializer {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final String encryptedText = super.deserialize(p, ctxt);
            
            if (encryptedText != null) {
                return decodeText(encryptedText);
            } else {
                return null;
            }
        }
        
    }
    
    private static String decodeText(final String text) {
        final ByteBuffer encryptedBytes = ByteBuffer.wrap(Base64.getDecoder().decode(text));
        
        final byte[] iv = new byte[IV_LENGTH_BYTE];
        encryptedBytes.get(iv);
        
        final byte[] salt = new byte[SALT_LENGTH_BYTE];
        encryptedBytes.get(salt);
        
        final byte[] cipherText = new byte[encryptedBytes.remaining()];
        encryptedBytes.get(cipherText);
        
        try {
            final SecretKey aesKey = getAESKeyForSecret(ENCODING_SECRET, salt);
            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] plainText = cipher.doFinal(cipherText);
            
            return new String(plainText);
        } catch (final GeneralSecurityException e) {
            throw new ApplicationException("Error decoding encrypted text", e);
        }
    }
    
    private static String encodeText(final String text) {
        final byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
        final byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
        
        try {
            final SecretKey aesKey = getAESKeyForSecret(ENCODING_SECRET, salt);
            
            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] cipherText = cipher.doFinal(text.getBytes());
            
            final byte[] result = ByteBuffer.allocate(iv.length + salt.length + cipherText.length).put(iv).put(salt).put(cipherText).array();
            
            return Base64.getEncoder().encodeToString(result);
        } catch (final GeneralSecurityException e) {
            throw new ApplicationException("Error encoding text.", e);
        }
    }
    
    private static SecretKey getAESKeyForSecret(final char[] secret, final byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec spec = new PBEKeySpec(secret, salt, 65536, 256);
        
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
    
    private static byte[] getRandomNonce(final int numBytes) {
        final byte[] nonce = new byte[numBytes];
        SALT_GENERATOR.nextBytes(nonce);
        
        return nonce;
    }
    
}
