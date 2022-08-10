package com.bootest.aws;

import java.util.HashMap;
import java.util.Map;

import com.bootest.model.Account;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

@Slf4j
@Component
public class Ec2ClientManager {

    public Ec2Client getEc2WithAccount(Region region, Account account) {
        Map<String, Ec2Client> clients = new HashMap<String, Ec2Client>();
        if (clients.isEmpty()) {
            clients.put(
                account.getAccountId(), 
                Ec2Client.builder().credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            account.getAccessKey(), account.getSecretKey())))
                .region(region)
                .build());
            log.info("Ec2Clients: {}", clients.keySet());
        }
        return clients.get(account.getAccountId());
    }
}
