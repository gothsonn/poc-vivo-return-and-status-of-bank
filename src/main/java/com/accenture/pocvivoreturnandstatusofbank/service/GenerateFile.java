package com.accenture.pocvivoreturnandstatusofbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateFile {

    @Value("${azure.storage.blob.local-path}")
    protected String localPath;

    public String generate(String data, String id) throws IOException {
//        String fileName = id +"-"+ System.currentTimeMillis() + ".txt";
        String fileName = id +".txt";
//        new File(localPath + fileName);
        log.info("generate-Encripted",data);
        log.info("generate-Id",id);
        FileWriter writer = new FileWriter(getLocalPath() + fileName);
        writer.write(data);
        writer.close();
        return fileName;
    }

    public String readFile( String fileName) throws IOException {
        log.info("\nreadFile \n\t " + getLocalPath() + fileName);
        FileReader reader = new FileReader(getLocalPath() + fileName);
        String  contentFile = readAllCharactersOneByOne(reader);
        log.info("\nreadFile and contentFile \n\t " + contentFile);
        return contentFile;
    }
    public static String readAllCharactersOneByOne(Reader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        int nextChar;
        while ((nextChar = reader.read()) != -1) {
            content.append((char) nextChar);
        }
        return String.valueOf(content);
    }

    public String getLocalPath() {
        return localPath;
    }
}
