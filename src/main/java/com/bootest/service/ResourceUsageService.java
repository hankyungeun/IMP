package com.bootest.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bootest.dto.GetMonthlyUsageDto;
import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.model.ResourceUsage;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResourceUsageService {

    private final ObjectMapper mapper;

    public List<GetMonthlyUsageDto> findAllByMonthAvg(String dateFrom, String dateTo, List<ResourceUsage> usages)
            throws JsonMappingException, JsonProcessingException {

        List<GetMonthlyUsageDto> results = new ArrayList<>();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);

        if (dateFrom != null && dateTo != null) {
            start = LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE);
            end = LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE);
        }

        List<LocalDate> dates = start.datesUntil(end).collect(Collectors.toList());

        Set<String> dateSet = new HashSet<>();

        for (LocalDate ld : dates) {
            String[] ldArr = ld.toString().split("-");
            dateSet.add(ldArr[0] + "-" + ldArr[1]);
        }

        List<String> resourceIds = usages.stream().map(ResourceUsage::getResourceId).collect(Collectors.toList());

        for (String resourceId : resourceIds) {
            GetMonthlyUsageDto resultData = new GetMonthlyUsageDto();

            String accountId = null;
            String accountName = null;
            String region = null;
            String resourceName = null;
            String os = null;
            String instanceType = null;

            List<StatisticDataValueDto> cpuAvgs = new ArrayList<>();
            List<StatisticDataValueDto> memAvgs = new ArrayList<>();
            List<StatisticDataValueDto> diskAvgs = new ArrayList<>();
            List<StatisticDataValueDto> netInAvgs = new ArrayList<>();
            List<StatisticDataValueDto> netOutAvgs = new ArrayList<>();

            for (ResourceUsage ru : usages) {
                if (ru.getResourceId().equals(resourceId)) {
                    accountId = ru.getAccountId();
                    accountName = ru.getAccountName();
                    region = ru.getRegion();
                    resourceName = ru.getResourceName();
                    os = ru.getOs();
                    instanceType = ru.getInstanceType();

                    for (String date : dateSet) {
                        String[] dateArr = date.split("-");

                        Short year = Short.parseShort(dateArr[0]);
                        Short month = Short.parseShort(dateArr[1]);

                        if (ru.getAnnually() == year && ru.getMonthly() == month) {

                            if (ru.getDataType().equals(UsageDataType.CPU)) {
                                StatisticDataDto cpuAvg = mapper.readValue(ru.getAverage(),
                                        StatisticDataDto.class);
                                cpuAvgs.addAll(cpuAvg.getData());
                            }

                            if (ru.getDataType().equals(UsageDataType.MEMORY)) {
                                StatisticDataDto memAvg = mapper.readValue(ru.getAverage(),
                                        StatisticDataDto.class);
                                memAvgs.addAll(memAvg.getData());
                            }

                            if (ru.getDataType().equals(UsageDataType.DISK)) {
                                StatisticDataDto diskAvg = mapper.readValue(ru.getAverage(),
                                        StatisticDataDto.class);
                                diskAvgs.addAll(diskAvg.getData());
                            }

                            if (ru.getDataType().equals(UsageDataType.NET_IN)) {
                                StatisticDataDto netInAvg = mapper.readValue(ru.getAverage(),
                                        StatisticDataDto.class);
                                netInAvgs.addAll(netInAvg.getData());
                            }

                            if (ru.getDataType().equals(UsageDataType.NET_OUT)) {
                                StatisticDataDto netOutAvg = mapper.readValue(ru.getAverage(),
                                        StatisticDataDto.class);
                                netOutAvgs.addAll(netOutAvg.getData());
                            }
                        }
                    }
                }
            }

            if (!cpuAvgs.isEmpty()) {
                resultData.setCpuAvg(getAverageByMonth(cpuAvgs, dateSet));
            } else {
                continue;
            }

            if (!memAvgs.isEmpty()) {
                resultData.setMemAvg(getAverageByMonth(memAvgs, dateSet));
            }

            if (!diskAvgs.isEmpty()) {
                resultData.setDiskAvg(getAverageByMonth(diskAvgs, dateSet));
            }

            if (!netInAvgs.isEmpty()) {
                resultData.setNetInAvg(getAverageByMonth(netInAvgs, dateSet));
            }

            if (!netOutAvgs.isEmpty()) {
                resultData.setNetOutAvg(getAverageByMonth(netOutAvgs, dateSet));
            }

            resultData.setAccountId(accountId);
            resultData.setAccountName(accountName);
            resultData.setRegion(region);
            resultData.setResourceId(resourceId);
            resultData.setResourceName(resourceName);
            resultData.setOs(os);
            resultData.setInstanceType(instanceType);

            results.add(resultData);
        }
        return results;
    }

    public List<StatisticDataValueDto> getAverageByMonth(List<StatisticDataValueDto> statData, Set<String> dateSet) {

        List<StatisticDataValueDto> results = new ArrayList<>();

        Map<String, Double> dataMap = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();

        if (statData != null && !statData.isEmpty()) {

            for (String date : dateSet) {
                String[] dateArr = date.split("-");
                String month = dateArr[0] + "-" + dateArr[1];
                for (StatisticDataValueDto data : statData) {

                    if (data.getTime().contains(month)) {
                        Double value = data.getValue();
                        if (dataMap.get(month) != null) {
                            value += dataMap.get(month);
                        }
                        dates.add(month);
                        dataMap.put(month, value);
                    }

                }
            }

            Map<String, Long> dateCounts = dates.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            for (Entry<String, Double> entry : dataMap.entrySet()) {
                Double resultValue = 0d;

                if (dateCounts.containsKey(entry.getKey())) {
                    resultValue = entry.getValue() / dateCounts.get(entry.getKey());
                }

                StatisticDataValueDto resultData = new StatisticDataValueDto();
                resultData.setTime(entry.getKey());
                resultData.setValue(resultValue);
                results.add(resultData);
            }
        }
        return results;
    }

}
