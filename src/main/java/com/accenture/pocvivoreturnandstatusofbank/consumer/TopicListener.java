package com.accenture.pocvivoreturnandstatusofbank.consumer;


import com.accenture.pocvivoreturnandstatusofbank.model.FinancialAccountCreateEvent;
import com.accenture.pocvivoreturnandstatusofbank.service.BlobService;
import com.accenture.pocvivoreturnandstatusofbank.service.EncryptedFinancialAccount;
import com.accenture.pocvivoreturnandstatusofbank.service.GenerateFile;
import com.accenture.pocvivoreturnandstatusofbank.service.SftpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
@Service
public class TopicListener {

    @Value("${topic.name.consumer}")
    private String topicName;

    protected BlobService blobService;

    protected EncryptedFinancialAccount encryptedFinancialAccount;
    protected GenerateFile generateFile;
    private final ObjectMapper objectMapper;

    private SftpService sftpService;

    @Autowired
    public TopicListener(BlobService blobService, EncryptedFinancialAccount encryptedFinancialAccount, GenerateFile generateFile, ObjectMapper objectMapper, SftpService sftpService) {
        this.blobService = blobService;
        this.encryptedFinancialAccount = encryptedFinancialAccount;
        this.generateFile = generateFile;
        this.objectMapper = objectMapper;
        this.sftpService = sftpService;
    }

    @KafkaListener(topics = "${topic.name.consumer}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> payload){
        log.info("Tópico: {}", topicName);
        log.info("key: {}", payload.key());
        log.info("Headers: {}", payload.headers());
        log.info("Partion: {}", payload.partition());
        log.info("Order: {}", payload.value());
        try {

            FinancialAccountCreateEvent consumeToGenerateFile =  objectMapper.readValue(payload.value(), FinancialAccountCreateEvent.class);
            String  encryptedString = encryptedFinancialAccount.objectToStringEncoded(consumeToGenerateFile.getPayload().getFinancialAccount());
            String id = consumeToGenerateFile.getPayload().getFinancialAccount().getId();
            String filename = generateFile.generate(encryptedString,id);
            sftpService.uploadFile(filename);
            log.info("FinancialAccountCreateEvent: {}", encryptedString);

        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);

        } catch (JSchException e) {
            throw new RuntimeException(e);
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }
    }

}