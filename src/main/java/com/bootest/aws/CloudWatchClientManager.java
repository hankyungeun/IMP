package com.bootest.aws;

import java.util.HashMap;
import java.util.Map;

import com.bootest.model.Account;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Component
public class CloudWatchClientManager {
    
    public CloudWatchClient getCwc(Region region, Account account) {

        Map<String, CloudWatchClient> clients = new HashMap<String, CloudWatchClient>();

        if (clients.isEmpty()) {
            clients.put(
                account.getAccountId(), 
                CloudWatchClient.builder()
                    .credentialsProvider(
                        StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                account.getAccessKey(), account.getSecretKey())))
                        .region(region)
                    .build());
        }

        return clients.get(account.getAccountId());
    }

}
