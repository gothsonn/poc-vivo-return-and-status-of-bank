package com.accenture.PocVivoReturnAndStatusOfBank.service;

import com.accenture.PocVivoReturnAndStatusOfBank.dao.FinancialAccountDao;
import com.accenture.PocVivoReturnAndStatusOfBank.model.FinancialAccount;
import com.accenture.PocVivoReturnAndStatusOfBank.model.FinancialAccountUpdate;
import com.accenture.PocVivoReturnAndStatusOfBank.topics.producer.TopicProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpServiceVivo {

    @Value("${sftp.connection.host}")
    private String host;

    @Value("${sftp.connection.user.vivo}")
    private String userVivo;

    @Value("${sftp.connection.password}")
    private String password;

    protected GenerateFile generateFile;
    protected EncryptedFinancialAccount encryptedFinancialAccount;

    private BlobService blobService;

    private TopicProducer topicProducer;

    private FinancialAccountDao financialAccountDao;

    private ObjectMapper objectMapper;

    @Autowired
    public SftpServiceVivo(
            GenerateFile generateFile,
            EncryptedFinancialAccount encryptedFinancialAccount,
            BlobService blobService,
            TopicProducer topicProducer, FinancialAccountDao financialAccountDao, ObjectMapper objectMapper) {
        this.generateFile = generateFile;
        this.encryptedFinancialAccount = encryptedFinancialAccount;
        this.blobService = blobService;
        this.topicProducer = topicProducer;
        this.financialAccountDao = financialAccountDao;
        this.objectMapper = objectMapper;
    }

    private ChannelSftp setupJsch() throws JSchException {
        JSch jsch = new JSch();

        Session jschSession = jsch.getSession(userVivo, host);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(password);
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }

    public String uploadFile(String fileName) throws JSchException, SftpException {
        String localPath = generateFile.getLocalPath();
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        String localFile = localPath + fileName;
        String remoteDir = "/";
        channelSftp.put(localFile, remoteDir + fileName);
        channelSftp.exit();
        deleteFile(fileName);
        return "Uploading to Blob storage vivo success";
    }

    public String downloadFile(String id) throws JSchException, SftpException, IOException {
        String localPath = generateFile.getLocalPath();
        String fileName = id +".txt";
        log.info("\nDownloading blob to\n\t " + localPath + fileName);
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        String localDir = localPath + fileName.replace(".txt", "-ret.txt");
        channelSftp.get(fileName, localDir);
        String contentFile = generateFile.readFile(fileName.replace(".txt", "-ret.txt"));
        channelSftp.exit();
        String financialAccount = encryptedFinancialAccount.decryptedString(contentFile);
//        deleteFile(fileName);
        return financialAccount;
    }

    public void deleteFile(String fileName){
        String localPath = generateFile.getLocalPath();
        log.info("\nDeleting blob to\n\t " + localPath + fileName);
        File deleteFile = new File(localPath + fileName);
        deleteFile.delete();
    }

    public void deleteRemoteFile(String fileName) throws JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        log.info("\nDeleting remote blob to\n\t " + fileName);
        channelSftp.rm(fileName);
        channelSftp.exit();
    }

    public void getDirectoryList() throws JSchException, SftpException, IOException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        @SuppressWarnings("null")
        Vector<ChannelSftp.LsEntry> filesList = channelSftp.ls("*.txt");
        List<String> listNameFile = new ArrayList<>();
        List<String> listFinancialAccount = new ArrayList<>();

        log.info("filesList size:" + filesList.size());
        if(filesList != null) {
            for (ChannelSftp.LsEntry entry : filesList) {
                if(!entry.getFilename().contains("-ret.txt")){
                    listNameFile.add(entry.getFilename());
                }
            }
        }


        for(String fileName : listNameFile){
            var id = fileName.split(".txt")[0];
            listFinancialAccount.add(downloadFile(id));
        }

        channelSftp.exit();

        for(String fileName : listNameFile){
            String file = fileName.replace(".txt", "-ret.txt");
            uploadFile(file);
            deleteRemoteFile(fileName);
        }


        for (String account : listFinancialAccount){
            FinancialAccount financialAccount = objectMapper.readValue(account, FinancialAccount.class);
            topicProducer.send(financialAccount);
            FinancialAccountUpdate accountUpdate = objectMapper.readValue(account, FinancialAccountUpdate.class);
            financialAccountDao.updateFinancialAccount(financialAccount.getId(), accountUpdate);
        }


    }
}
