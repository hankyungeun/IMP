package com.bootest.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.dto.usage.SummaryResponse;
import com.bootest.model.Account;
import com.bootest.model.ResultObject;
import com.bootest.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bootest.dto.GetInstanceStateDto;
import com.bootest.dto.GetMonthlyUsageDto;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import com.bootest.service.ResourceUsageService;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("/usage")
public class ResourceUsageController {

    private final ResourceUsageService service;
    private final ResourceUsageRepo resourceUsageRepo;
    private final ObjectMapper mapper;

    @GetMapping
    public List<GetMonthlyUsageDto> findAllByMonthAvg(
            HttpServletRequest request,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) String resourceState,
            @RequestParam(required = false) String lifeCycle,
            @RequestParam(required = false) String imageId,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String instanceType,
            @RequestParam(required = false) UsageDataType dataType,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) throws JsonMappingException, JsonProcessingException {
        SearchBuilder<ResourceUsage> searchBuilder = SearchBuilder.builder();

        User user = (User) request.getSession().getAttribute("loginUser");

        List<Account> accounts = new ArrayList<>();

        if (user != null) {
            accounts.addAll(user.getAccounts());
        }

        if (!accounts.isEmpty()) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accounts.stream().map(Account::getAccountId).collect(Collectors.toSet()));
        }

        if (accountName != null) {
            searchBuilder.with("accountName", SearchOperationType.EQUAL, accountName);
        }

        if (region != null) {
            searchBuilder.with("region", SearchOperationType.EQUAL, region);
        }

        if (resourceId != null) {
            searchBuilder.with("resourceId", SearchOperationType.EQUAL, resourceId);
        }

        if (resourceName != null) {
            searchBuilder.with("resourceName", SearchOperationType.EQUAL, resourceName);
        }

        if (resourceState != null) {
            searchBuilder.with("resourceState", SearchOperationType.EQUAL, resourceState);
        }

        if (lifeCycle != null) {
            searchBuilder.with("lifeCycle", SearchOperationType.EQUAL, lifeCycle);
        }

        if (imageId != null) {
            searchBuilder.with("imageId", SearchOperationType.EQUAL, imageId);
        }

        if (os != null) {
            searchBuilder.with("os", SearchOperationType.EQUAL, os);
        }

        if (instanceType != null) {
            searchBuilder.with("instanceType", SearchOperationType.EQUAL, instanceType);
        }

        if (dataType != null) {
            searchBuilder.with("dataType", SearchOperationType.EQUAL, dataType);
        }

        if (dateFrom != null) {
            String[] dateArray = dateFrom.split("-");
            searchBuilder.with("annually", SearchOperationType.GREATER_THAN_OR_EQUAL, dateArray[0]);
            searchBuilder.with("monthly", SearchOperationType.GREATER_THAN_OR_EQUAL, dateArray[1]);
        }

        if (dateTo != null) {
            String[] dateArray = dateTo.split("-");
            searchBuilder.with("annually", SearchOperationType.LESS_THAN_OR_EQUAL, dateArray[0]);
            searchBuilder.with("monthly", SearchOperationType.LESS_THAN_OR_EQUAL, dateArray[1]);
        }

        List<ResourceUsage> usages = resourceUsageRepo.findAll(searchBuilder.build());

        return service.getAllAvgByMonth(dateFrom, dateTo, usages);
    }

    @GetMapping("/resource")
    public List<GetMonthlyUsageDto> findAllByResource(
            HttpServletRequest request,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) String resourceState,
            @RequestParam(required = false) String lifeCycle,
            @RequestParam(required = false) String imageId,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String instanceType,
            @RequestParam(required = false) UsageDataType dataType) throws JsonMappingException, JsonProcessingException {
        SearchBuilder<ResourceUsage> searchBuilder = SearchBuilder.builder();
        String dateFrom = "2023-01-01";
        String dateTo = "2023-12-31";

        User user = (User) request.getSession().getAttribute("loginUser");

        List<Account> accounts = new ArrayList<>();

        if (user != null) {
            accounts.addAll(user.getAccounts());
        }

        if (!accounts.isEmpty()) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accounts.stream().map(Account::getAccountId).collect(Collectors.toSet()));
        }

        if (accountName != null) {
            searchBuilder.with("accountName", SearchOperationType.EQUAL, accountName);
        }

        if (region != null) {
            searchBuilder.with("region", SearchOperationType.EQUAL, region);
        }

        if (resourceId != null) {
            searchBuilder.with("resourceId", SearchOperationType.EQUAL, resourceId);
        }

        if (resourceName != null) {
            searchBuilder.with("resourceName", SearchOperationType.EQUAL, resourceName);
        }

        if (resourceState != null) {
            searchBuilder.with("resourceState", SearchOperationType.EQUAL, resourceState);
        }

        if (lifeCycle != null) {
            searchBuilder.with("lifeCycle", SearchOperationType.EQUAL, lifeCycle);
        }

        if (imageId != null) {
            searchBuilder.with("imageId", SearchOperationType.EQUAL, imageId);
        }

        if (os != null) {
            searchBuilder.with("os", SearchOperationType.EQUAL, os);
        }

        if (instanceType != null) {
            searchBuilder.with("instanceType", SearchOperationType.EQUAL, instanceType);
        }

        if (dataType != null) {
            searchBuilder.with("dataType", SearchOperationType.EQUAL, dataType);
        }

        if (dateFrom != null) {
            String[] dateArray = dateFrom.split("-");
            searchBuilder.with("annually", SearchOperationType.GREATER_THAN_OR_EQUAL, dateArray[0]);
            searchBuilder.with("monthly", SearchOperationType.GREATER_THAN_OR_EQUAL, dateArray[1]);
        }

        if (dateTo != null) {
            String[] dateArray = dateTo.split("-");
            searchBuilder.with("annually", SearchOperationType.LESS_THAN_OR_EQUAL, dateArray[0]);
            searchBuilder.with("monthly", SearchOperationType.LESS_THAN_OR_EQUAL, dateArray[1]);
        }

        List<ResourceUsage> usages = resourceUsageRepo.findAll(searchBuilder.build());

        return service.findAllByResource(dateFrom, dateTo, usages);
    }

    @GetMapping("/state")
    public GetInstanceStateDto findAllInstanceState(HttpServletRequest request) {

        User user = (User) request.getSession().getAttribute("loginUser");

        List<Account> accounts = new ArrayList<>();

        if (user != null) {
            accounts.addAll(user.getAccounts());
        }

        LocalDate now = LocalDate.of(2023, 3, 1);
        String[] nowArr = now.toString().split("-");

        SearchBuilder<ResourceUsage> searchBuilder = SearchBuilder.builder();

        if (!accounts.isEmpty()) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accounts.stream().map(Account::getAccountId).collect(Collectors.toSet()));
        }

        searchBuilder.with("dataType", SearchOperationType.EQUAL, UsageDataType.CPU);

        searchBuilder.with("annually", SearchOperationType.EQUAL, Short.parseShort(nowArr[0]));
        searchBuilder.with("monthly", SearchOperationType.EQUAL, Short.parseShort(nowArr[1]));

        List<ResourceUsage> usages = resourceUsageRepo.findAll(searchBuilder.build());

        Integer running = 0;
        Integer stopped = 0;
        Integer terminated = 0;
        Integer spot = 0;
        Integer onDemand = 0;

        for (ResourceUsage usage : usages) {

            if (usage.getLifeCycle().equals("spot")) {
                spot++;
            } else {
                onDemand++;
            }

            if (usage.getResourceState().equals("running") || usage.getResourceState().equals("pending")) {
                running++;
            } else if (usage.getResourceState().equals("stopped") || usage.getResourceState().equals("stopping")) {
                stopped++;
            } else if (usage.getResourceState().equals("terminated")
                    || usage.getResourceState().equals("shutting-down")) {
                terminated++;
            }
        }
        GetInstanceStateDto state = new GetInstanceStateDto();
        state.setOnDemand(onDemand);
        state.setRunning(running);
        state.setSpot(spot);
        state.setTerminated(terminated);
        state.setStopped(stopped);

        return state;
    }

    // ip 주소/usages/summaries?param이름=skdflksj
    @GetMapping("/summaries")
    public ResponseEntity<ResultObject> getSummaries(
            HttpServletRequest request,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) throws JsonMappingException, JsonProcessingException {
        ResultObject result = new ResultObject();

        SearchBuilder<ResourceUsage> searchBuilder = SearchBuilder.builder();

        // 날짜 파라미터로 전달 하도록 완성되면 지우기!!!!!
        dateFrom = "2023-01-01";
        dateTo = "2023-12-31";
        // 여기까지

        User user = (User) request.getSession().getAttribute("loginUser");

        List<Account> accounts = new ArrayList<>();

        if (user != null) {
            accounts.addAll(user.getAccounts());
        }

        if (!accounts.isEmpty()) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accounts.stream().map(Account::getAccountId).collect(Collectors.toSet()));
        } else {
            throw new IllegalArgumentException("NONE");
        }

        if (dateFrom != null) {
            String[] dateArray = dateFrom.split("-");
            searchBuilder.with("annually", SearchOperationType.GREATER_THAN_OR_EQUAL, dateArray[0]);
            searchBuilder.with("monthly", SearchOperationType.GREATER_THAN_OR_EQUAL, dateArray[1]);
        }

        if (dateTo != null) {
            String[] dateArray = dateTo.split("-");
            searchBuilder.with("annually", SearchOperationType.LESS_THAN_OR_EQUAL, dateArray[0]);
            searchBuilder.with("monthly", SearchOperationType.LESS_THAN_OR_EQUAL, dateArray[1]);
        }

        List<ResourceUsage> usages = resourceUsageRepo.findAll(searchBuilder.build());

        Float totalAvgCpu = 0f;
        Float totalAvgNetIn = 0f;
        Float totalAvgNetOut = 0f;

        int cpuCnt = 0;
        int netInCnt = 0;
        int netOutCnt = 0;

        Map<String, Float> cpuDailyAvgs = new TreeMap<>();
        Map<String, Float> netInDailyAvgs = new TreeMap<>();
        Map<String, Float> netOutDailyAvgs = new TreeMap<>();


        for (ResourceUsage usage : usages) {

            StatisticDataDto avgStat = mapper.readValue(usage.getAverage(), StatisticDataDto.class);
            List<StatisticDataValueDto> data = avgStat.getData();

            if (usage.getDataType().equals(UsageDataType.CPU)) {
                cpuDailyAvgs.putAll(getDailyAverage(data));
                for (StatisticDataValueDto stat : data) {
                    totalAvgCpu += stat.getValue().floatValue();
                    cpuCnt++;
                }
            } else if (usage.getDataType().equals(UsageDataType.NET_IN)) {
                netInDailyAvgs.putAll(getDailyAverage(data));
                for (StatisticDataValueDto stat : data) {
                    totalAvgNetIn += stat.getValue().floatValue();
                    netInCnt++;
                }
            } else if (usage.getDataType().equals(UsageDataType.NET_OUT)) {
                netOutDailyAvgs.putAll(getDailyAverage(data));
                for (StatisticDataValueDto stat : data) {
                    totalAvgNetOut += stat.getValue().floatValue();
                    netOutCnt++;
                }
            }

        }

        result.setResult(true);
        result.setData(new SummaryResponse(totalAvgCpu / cpuCnt, totalAvgNetIn / netInCnt, totalAvgNetOut / netOutCnt, cpuDailyAvgs, netInDailyAvgs, netOutDailyAvgs));

        return new ResponseEntity<ResultObject>(result, HttpStatus.OK);
    }

    public Map<String, Float> getDailyAverage(List<StatisticDataValueDto> usageData) {

        Map<String, Float> dateAndUsages = new TreeMap<>();
        Map<String, Float> dateAndCounts = new TreeMap<>();

        for (StatisticDataValueDto data : usageData) {

            Float cnt = 1f;
            Float usage = data.getValue().floatValue();
            String[] timeArr = data.getTime().split("T");

            String date = null;

            if (timeArr.length > 1) {
                date = timeArr[0];
            }

            if (date != null) {

                if (dateAndUsages.containsKey(date)) {
                    usage += usage + dateAndUsages.get(date);
                }

                if (dateAndCounts.containsKey(date)) {
                    cnt += cnt + dateAndCounts.get(date);
                }

                dateAndUsages.put(date, usage);
                dateAndCounts.put(date, cnt);
            }
        }

        dateAndCounts.forEach((k, v) -> dateAndUsages.computeIfPresent(k, (key, value) -> value / v));

        return dateAndUsages;
    }
}
