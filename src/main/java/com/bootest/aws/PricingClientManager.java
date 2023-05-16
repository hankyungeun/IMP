package com.bootest.aws;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.bootest.model.Account;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.pricing.PricingClient;

@Slf4j
@Component
public class PricingClientManager {

    //특정 계정에 대한 Pricing클라이언트를 반환
    public PricingClient getPricingClient(Account account) {
        Map<String, PricingClient> clients = new HashMap<String, PricingClient>();
        if (clients.isEmpty()) {
            clients.put(
                    account.getAccountId(),
                    PricingClient.builder().credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(
                                            account.getAccessKey(), account.getSecretKey())))
                            .region(Region.US_EAST_1)
                            .build());
            log.info("Ec2Clients: {}", clients.keySet());
        }
        return clients.get(account.getAccountId());
    }

}
