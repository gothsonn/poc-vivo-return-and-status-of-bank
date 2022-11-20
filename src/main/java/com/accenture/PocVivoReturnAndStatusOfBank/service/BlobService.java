package com.accenture.PocVivoReturnAndStatusOfBank.service;

import com.accenture.PocVivoReturnAndStatusOfBank.model.FinancialAccount;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlobService {

    protected String connectStr;

    protected String containerName;

    BlobServiceClient blobServiceClient;
    BlobContainerClient containerClient;
    protected GenerateFile generateFile;

    protected EncryptedFinancialAccount encryptedFinancialAccount;

    @Autowired
    public BlobService(@Value("${azure.storage.blob.connection-string}") String connectStr,@Value("${azure.storage.blob.container-name}") String containerName, GenerateFile generateFile, EncryptedFinancialAccount encryptedFinancialAccount){
        this.connectStr = connectStr;
        this.containerName = containerName;
        this.blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();
         this.containerClient = blobServiceClient.createBlobContainerIfNotExists(containerName);
        this.generateFile = generateFile;
        this.encryptedFinancialAccount = encryptedFinancialAccount;
    }

    public String uploadFile(String fileName) throws IOException {

        String localPath = generateFile.getLocalPath();

        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        log.info("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());


        // Upload the blob
        blobClient.uploadFromFile(localPath + fileName);

        deleteFile(fileName);

        return "Uploading to Blob storage success";
    }

    public FinancialAccount downloadFile(String id) throws IOException {
        String localPath = generateFile.getLocalPath();
        String fileName = id +".txt";
        log.info("\nDownloading blob to\n\t " + localPath + fileName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        blobClient.downloadToFile(localPath + fileName);
        String contentFile = generateFile.readFile(fileName);
        FinancialAccount financialAccount = encryptedFinancialAccount.objectToStringDecode(contentFile);
        deleteFile(fileName);
        return financialAccount;
    }

    public void deleteFile(String fileName){
        String localPath = generateFile.getLocalPath();
        log.info("\nDeleting blob to\n\t " + localPath + fileName);
        File deleteFile = new File(localPath + fileName);
        deleteFile.delete();
    }
}
