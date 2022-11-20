package com.accenture.PocVivoReturnAndStatusOfBank.service;

import com.accenture.PocVivoReturnAndStatusOfBank.model.FinancialAccount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EncryptedFinancialAccount {
    private final ObjectMapper objectMapper;

    public FinancialAccount objectToStringDecode(String account) throws JsonProcessingException {
        return objectMapper.readValue(decryptedString(account), FinancialAccount.class);
    }
    public String objectToStringEncoded(FinancialAccount account) throws JsonProcessingException {
        return encryptedString(objectMapper.writeValueAsString(account));
    }

    public String encryptedString(String accountString){
        return Base64.getEncoder().encodeToString(accountString.getBytes());
    }
    public String decryptedString(String accountString){
        byte[] decodedBytes = Base64.getDecoder().decode(accountString);
        return new String(decodedBytes);
    }
}
