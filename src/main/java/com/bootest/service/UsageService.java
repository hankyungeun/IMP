package com.bootest.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.dto.optimizer.UsageMetricDto;
import com.bootest.dto.usage.AverageResourceUsage;
import com.bootest.dto.usage.AverageUsageDto;
import com.bootest.dto.usage.UsageDateDto;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UsageService {

    private final ResourceUsageRepo resourceUsageRepo;
    private final ObjectMapper objectMapper;

    public List<AverageResourceUsage> sortAverage(List<AverageUsageDto> data) {
        List<AverageResourceUsage> results = new ArrayList<>();

        Set<String> instanceIds = data.stream().map(AverageUsageDto::getResourceId).collect(Collectors.toSet());

        for (String instanceId : instanceIds) {
            AverageResourceUsage sortedData = new AverageResourceUsage();
            List<UsageMetricDto> cpuUsageMetrics = null;
            List<UsageMetricDto> netInUsageMetrics = null;
            List<UsageMetricDto> netOutUsageMetrics = null;
            Double avgCpuUsage = 0d;
            Double avgNetIn = 0d;
            Double avgNetOut = 0d;
            for (AverageUsageDto usage : data) {
                if (usage.getResourceId().equals(instanceId)) {
                    sortedData.setAccountId(usage.getAccountId());
                    sortedData.setAccountName(usage.getAccountName());
                    sortedData.setResourceName(usage.getResourceName());
                    sortedData.setResourceState(usage.getResourceState());
                    sortedData.setLifeCycle(usage.getLifeCycle());
                    sortedData.setOs(usage.getOs());
                    sortedData.setInstanceType(usage.getInstanceType());
                    sortedData.setDate(usage.getDate());

                    if (usage.getUsageDataType().equals(UsageDataType.CPU)) {
                        cpuUsageMetrics = usage.getUsageMetrics();
                        avgCpuUsage = usage.getAvgUsage();
                    } else if (usage.getUsageDataType().equals(UsageDataType.NET_IN)) {
                        netInUsageMetrics = usage.getUsageMetrics();
                        avgNetIn = usage.getAvgUsage();
                    } else if (usage.getUsageDataType().equals(UsageDataType.NET_OUT)) {
                        netOutUsageMetrics = usage.getUsageMetrics();
                        avgNetOut = usage.getAvgUsage();
                    }
                }
            }
            sortedData.setResourceId(instanceId);
            sortedData.setCpuUsageMetrics(cpuUsageMetrics);
            sortedData.setNetInUsageMetrics(netInUsageMetrics);
            sortedData.setNetOutUsageMetrics(netOutUsageMetrics);
            sortedData.setAvgCpuUsage(avgCpuUsage);
            sortedData.setAvgNetIn(avgNetIn);
            sortedData.setAvgNetOut(avgNetOut);

            results.add(sortedData);
        }
        return results;
    }

    public List<AverageUsageDto> getAverage(
            List<ResourceUsage> usages,
            Instant from,
            Instant to) throws JsonMappingException, JsonProcessingException {
        List<AverageUsageDto> results = new ArrayList<>();
        for (ResourceUsage usage : usages) {

            List<UsageDateDto> dates = getDates(from, to);

            List<ResourceUsage> mergedUsages = getUsagesByPeriod(dates, usage);

            if (mergedUsages.isEmpty()) {
                continue;
            }

            List<StatisticDataDto> avgStatistics = new ArrayList<>();

            for (ResourceUsage vrmUsage : mergedUsages) {
                if (usage.getDataType().equals(UsageDataType.CPU)) {
                    avgStatistics.add(objectMapper.readValue(vrmUsage.getAverage(), StatisticDataDto.class));
                } else if (usage.getDataType().equals(UsageDataType.NET_IN)) {
                    avgStatistics.add(objectMapper.readValue(vrmUsage.getAverage(), StatisticDataDto.class));
                } else if (usage.getDataType().equals(UsageDataType.NET_OUT)) {
                    avgStatistics.add(objectMapper.readValue(vrmUsage.getAverage(), StatisticDataDto.class));
                }
            }

            Map<String, Double> avgMap = getAvgUsageMap(avgStatistics);

            Double avgValue = getAvgUsage(avgMap, from, to);

            List<UsageMetricDto> usageMetrics = getUsageMetrics(avgMap, from, to);

            AverageUsageDto data = new AverageUsageDto();
            data.setAccountId(usage.getAccountId());
            data.setAccountName(usage.getAccountName());
            data.setRegion(usage.getRegion());
            data.setResourceId(usage.getResourceId());
            data.setResourceName(usage.getResourceName());
            data.setResourceState(usage.getResourceState());
            data.setLifeCycle(usage.getLifeCycle());
            data.setOs(usage.getOs());
            data.setInstanceType(usage.getInstanceType());
            data.setDate(from + " ~ " + to);
            data.setUsageDataType(usage.getDataType());
            data.setUsageMetrics(usageMetrics);
            data.setAvgUsage(avgValue);
            results.add(data);
        }
        return results;
    }

    public List<UsageDateDto> getDates(Instant from, Instant to) {

        List<LocalDate> dates = LocalDate.ofInstant(from, ZoneId.systemDefault())
                .datesUntil(LocalDate.ofInstant(to.plus(1, ChronoUnit.DAYS), ZoneId.systemDefault()))
                .collect(Collectors.toList());

        Map<String, String> dateMap = new HashMap<>();

        for (LocalDate ld : dates) {
            String[] dateArr = ld.toString().split("-");

            dateMap.put(dateArr[0] + "-" + dateArr[1], "date");
        }

        List<UsageDateDto> results = new ArrayList<>();

        for (Entry<String, String> entry : dateMap.entrySet()) {
            String[] dateArr = entry.getKey().split("-");
            UsageDateDto dateData = new UsageDateDto();
            dateData.setYear(Short.parseShort(dateArr[0]));
            dateData.setMonth(Short.parseShort(dateArr[1]));
            results.add(dateData);
        }
        return results;
    }

    public List<ResourceUsage> getUsagesByPeriod(List<UsageDateDto> dates, ResourceUsage u) {
        List<ResourceUsage> results = new ArrayList<>();

        for (UsageDateDto date : dates) {
            ResourceUsage usage = resourceUsageRepo
                    .findByAccountIdAndRegionAndResourceIdAndAnnuallyAndMonthlyAndDataType(u.getAccountId(),
                            u.getRegion(), u.getResourceId(), date.getYear(), date.getMonth(), u.getDataType())
                    .orElse(null);

            if (usage != null) {
                results.add(usage);
            }
        }
        return results;
    }

    public Map<String, Double> getAvgUsageMap(List<StatisticDataDto> avgStatistics) {
        Map<String, Double> results = new LinkedHashMap<>();
        for (StatisticDataDto s : avgStatistics) {
            for (StatisticDataValueDto data : s.getData()) {
                results.put(data.getTime(), data.getValue());
            }
        }
        return results;
    }

    public Double getAvgUsage(Map<String, Double> avgUsageMap, Instant from, Instant to) {

        Double sum = 0d;
        int cnt = 0;

        if (avgUsageMap != null) {
            for (Entry<String, Double> entry : avgUsageMap.entrySet()) {
                Instant dateTime = Instant.parse(entry.getKey());

                if (dateTime.isAfter(from) && dateTime.isBefore(to)) {
                    cnt++;
                    sum += entry.getValue();
                }
            }
        }

        Double val = sum / cnt;
        if (!val.isNaN()) {
            BigDecimal bd = BigDecimal.valueOf(val);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            val = bd.doubleValue();
        }
        return val;
    }

    public List<UsageMetricDto> getUsageMetrics(Map<String, Double> avgUsageMap, Instant from, Instant to) {

        List<UsageMetricDto> results = new ArrayList<>();

        if (avgUsageMap != null) {

            Map<String, Double> filteredUsage = new TreeMap<>();

            for (Entry<String, Double> entry : avgUsageMap.entrySet()) {

                Instant dateTime = Instant.parse(entry.getKey());

                if (dateTime.isAfter(from) && dateTime.isBefore(to)) {
                    filteredUsage.put(entry.getKey(), entry.getValue());
                }
            }

            Map<LocalDate, Double> dates = new TreeMap<>();
            List<LocalDate> localDates = new ArrayList<>();

            for (Entry<String, Double> entry : filteredUsage.entrySet()) { // make a map of date and value

                LocalDate ld = Instant.parse(entry.getKey()).atZone(ZoneId.systemDefault()).toLocalDate();
                Double value = entry.getValue();
                localDates.add(ld);

                if (dates.containsKey(ld)) {
                    value = entry.getValue() + dates.get(ld);
                }
                dates.put(ld, value);
            }

            Map<LocalDate, Long> dateCounts = localDates.stream()
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            for (Entry<LocalDate, Double> entry : dates.entrySet()) {
                Double resultValue = 0d;
                if (dateCounts.containsKey(entry.getKey())) {
                    resultValue = entry.getValue() / dateCounts.get(entry.getKey());
                }
                UsageMetricDto um = new UsageMetricDto();
                um.setDate(entry.getKey().toString());
                um.setValue(resultValue.toString());
                results.add(um);
            }
        }
        return results;
    }

}
