package org.buratishkin.familyhub.auth.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class TextEncryptorService {
    private final TextEncryptor encryptor;

    public TextEncryptorService(
            @Value("${family-hub.security.encryptor.password}") String password,
            @Value("${family-hub.security.encryptor.salt}") String salt
    ) {
        this.encryptor = Encryptors.delux(password, salt);
    }

    public String encrypt(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        return encryptor.encrypt(rawToken);
    }

    public String decrypt(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isBlank()) {
            return null;
        }
        return encryptor.decrypt(encryptedToken);
    }
}
