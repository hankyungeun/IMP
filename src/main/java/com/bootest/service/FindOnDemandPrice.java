package com.bootest.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bootest.aws.PricingClientManager;
import com.bootest.dto.pricing.InstanceTypeSpecDto;
import com.bootest.dto.pricing.PriceDimensionDetailsDto;
import com.bootest.model.Account;
import com.bootest.repository.AccountRepo;
import com.bootest.util.Objects;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.pricing.PricingClient;
import software.amazon.awssdk.services.pricing.model.*;

@Slf4j
@RequiredArgsConstructor
@Service
//온디맨드 가격 정보를 조회하는 서비스
public class FindOnDemandPrice {

        private final AccountRepo accountRepo;
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final AWSRegion awsRegion;
        private final PricingClientManager pccm;

        @Value("${task.master-account}")
        private String masterAccount;

        /**
         * 
         * @param regionCode
         * @param usage      Linux = "RunInstances", Windows = "RunInstances:0002"
         * @param account
         * @param os
         * @return
         * @throws Exception
         */

        //특정 지역과 계정에 대한 온디맨드 인스턴스의 가격 정보 조회
        public List<InstanceTypeSpecDto> getAllOdPrice(String regionCode, Account account, String os) throws Exception {

                List<InstanceTypeSpecDto> results = new ArrayList<>();

                String nextToken = null;

                PricingClient pc = pccm.getPricingClient(account);

                Filter f1 = Filter.builder().type(FilterType.TERM_MATCH).field("regionCode").value(regionCode).build();
                Filter f2 = Filter.builder().type(FilterType.TERM_MATCH).field("capacitystatus").value("Used").build();

                String operatingSystem = os == "Linux" ? "Linux" : "Windows";
                String usageType = os == "Linux" ? "RunInstances" : "RunInstances:0002";

                Filter f3 = Filter.builder().type(FilterType.TERM_MATCH).field("operatingSystem").value(operatingSystem)
                                .build();
                Filter f4 = Filter.builder().type(FilterType.TERM_MATCH).field("operation").value(usageType).build();
                Filter f5 = Filter.builder().type(FilterType.TERM_MATCH).field("tenancy").value("shared").build();

                List<Filter> filters = new ArrayList<>();
                filters.add(f1);
                filters.add(f2);
                filters.add(f3);
                filters.add(f4);
                filters.add(f5);

                try {
                        do {
                                GetProductsRequest request = GetProductsRequest.builder()
                                                .formatVersion("aws_v1")
                                                .serviceCode("AmazonEC2")
                                                .maxResults(100)
                                                .nextToken(nextToken)
                                                .filters(filters)
                                                .build();

                                GetProductsResponse response = pc.getProducts(request);

                                for (String s : response.priceList()) {

                                        Map<String, Object> priceListMap = Objects.convertByJson(s,
                                                        new TypeReference<Map<String, Object>>() {
                                                        });

                                        Map<String, Object> productMap = Objects.convert(priceListMap.get("product"),
                                                        new TypeReference<Map<String, Object>>() {
                                                        });
                                        Map<String, Object> attributesMap = Objects.convert(
                                                        productMap.get("attributes"),
                                                        new TypeReference<Map<String, Object>>() {
                                                        });

                                        Map<String, Object> termsMap = Objects.convert(priceListMap.get("terms"),
                                                        new TypeReference<Map<String, Object>>() {
                                                        });
                                        Map<String, Object> onDemandMap = Objects.convert(termsMap.get("OnDemand"),
                                                        new TypeReference<Map<String, Object>>() {
                                                        });

                                        List<Object> odSpecification = new ArrayList<>();
                                        for (Entry<String, Object> entry : onDemandMap.entrySet()) {
                                                odSpecification.add(entry.getValue());
                                        }

                                        Map<String, Object> tokenMap = Objects.convert(odSpecification.get(0),
                                                        new TypeReference<Map<String, Object>>() {
                                                        });
                                        Map<String, Object> priceDimensionsMap = Objects.convert(
                                                        tokenMap.get("priceDimensions"),
                                                        new TypeReference<Map<String, Object>>() {
                                                        });

                                        List<Object> pdSpecification = new ArrayList<>();
                                        for (Entry<String, Object> entry : priceDimensionsMap.entrySet()) {
                                                pdSpecification.add(entry.getValue());
                                        }

                                        PriceDimensionDetailsDto pdd = objectMapper.convertValue(pdSpecification.get(0),
                                                        PriceDimensionDetailsDto.class);

                                        InstanceTypeSpecDto its = new InstanceTypeSpecDto();
                                        its.setRegion(regionCode);
                                        its.setInstanceType(attributesMap.get("instanceType").toString());
                                        its.setOs(os);
                                        its.setCost(pdd.getPricePerUnit().getUsd());

                                        results.add(its);
                                }
                                nextToken = response.nextToken();
                        } while (nextToken != null);
                } catch (PricingException e) {
                        log.error("Instance Type Info Request Failed (Reason: {})", e.getMessage(), e);
                }
                return results;
        }
}
