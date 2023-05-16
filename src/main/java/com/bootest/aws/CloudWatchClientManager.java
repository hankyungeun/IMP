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
//CloudWatch를 관리하기 위한 컴포넌트
public class CloudWatchClientManager {

    //주어진 리전과 계정을 기반으로 CloudWatch클라이언트를 반환
    public CloudWatchClient getCwc(Region region, Account account) {

        Map<String, CloudWatchClient> clients = new HashMap<String, CloudWatchClient>();

        //키값을 이용해 aws기본인증 객체를 생성하고 이를 사용해 CloudWatch클라이언트를 빌드
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

        //계정 아이디를 키로 사용해 'clients'에서 해당 계정의 CloudWatch클라이언트를 반환
        return clients.get(account.getAccountId());
    }

}
