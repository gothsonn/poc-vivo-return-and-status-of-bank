package com.accenture.pocvivoreturnandstatusofbank.service;

import com.accenture.pocvivoreturnandstatusofbank.model.FinancialAccount;
import com.jcraft.jsch.*;
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
public class SftpService {

    @Value("${sftp.connection.host}")
    private String host;

    @Value("${sftp.connection.user}")
    private String user;

    @Value("${sftp.connection.password}")
    private String password;

    protected GenerateFile generateFile;
    protected EncryptedFinancialAccount encryptedFinancialAccount;

    @Autowired
    public SftpService(GenerateFile generateFile, EncryptedFinancialAccount encryptedFinancialAccount) {
        this.generateFile = generateFile;
        this.encryptedFinancialAccount = encryptedFinancialAccount;
    }

    private ChannelSftp setupJsch() throws JSchException {
        JSch jsch = new JSch();
//        jsch.setKnownHosts(knowHosts);
        Session jschSession = jsch.getSession(user, host);
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
        return "Uploading to Blob storage success";
    }

    public FinancialAccount downloadFile(String id) throws JSchException, SftpException, IOException {
        String localPath = generateFile.getLocalPath();
        String fileName = id +".txt";
        log.info("\nDownloading blob to\n\t " + localPath + fileName);
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        String localDir = localPath + fileName;
        channelSftp.get(fileName, localDir);
        String contentFile = generateFile.readFile(fileName);
        channelSftp.exit();
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
