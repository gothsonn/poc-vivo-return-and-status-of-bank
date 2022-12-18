package com.accenture.PocVivoReturnAndStatusOfBank.controller;

import com.accenture.PocVivoReturnAndStatusOfBank.service.BlobService;
import com.accenture.PocVivoReturnAndStatusOfBank.service.SftpServiceVivo;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("blob")
public class BlobController {

    protected final BlobService blobService;

    private final SftpServiceVivo sftpServiceVivo;

    public BlobController(BlobService blobService, SftpServiceVivo sftpServiceVivo) {
        this.blobService = blobService;
        this.sftpServiceVivo = sftpServiceVivo;
    }

    @GetMapping("/getFiles")
    public String getFiles() throws JSchException, SftpException, IOException {
        sftpServiceVivo.getDirectoryList();
        return "Deu certo";
    }


//    @GetMapping("/testeDecode/{id}")
//    public String testeContro(@PathVariable("id") String id) {
//        return sftpServiceVivo.testeChamada(id);
//    }
}
