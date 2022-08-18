package com.bootest.task;

import java.util.UUID;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bootest.model.Account;
import com.bootest.model.User;
import com.bootest.repository.AccountRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class InstanceTask {

    private final AccountRepo accountRepo;

    // @Scheduled(cron = "20 16 * * * *")
    public void doInstanceCheck() {
        System.out.println("dddddddddddddddddddddddddd");
        Account account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setAccountId("1111");
        account.setUserId("1111");
        account.setName("1111");
        account.setRegions("1111");
        account.setAccessKey("1111");
        account.setSecretKey("1111");

        accountRepo.save(account);

    }

}
