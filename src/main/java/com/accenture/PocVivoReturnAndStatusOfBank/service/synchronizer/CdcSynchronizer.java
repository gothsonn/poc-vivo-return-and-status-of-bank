package com.accenture.PocVivoReturnAndStatusOfBank.service.synchronizer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CdcSynchronizer {

    @Autowired


    //@Scheduled(fixedDelay = 120000)
    public void synchronize() {
        log.info("Start to run the sync...");



        log.info("Finishing scheduled... ");
    }
}

