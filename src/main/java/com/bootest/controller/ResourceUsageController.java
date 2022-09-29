package com.bootest.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

@RequiredArgsConstructor
@RestController
@RequestMapping("/usage")
public class ResourceUsageController {

    private final ResourceUsageService service;
    private final ResourceUsageRepo resourceUsageRepo;

    @GetMapping
    public List<GetMonthlyUsageDto> findAllByMonthAvg(
            @RequestParam(required = false) String accountId,
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

        if (accountId != null) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accountId);
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
            @RequestParam(required = false) String accountId,
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

        if (accountId != null) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accountId);
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
    public GetInstanceStateDto findAllInstanceState() {

        LocalDate now = LocalDate.now();
        String[] nowArr = now.toString().split("-");

        SearchBuilder<ResourceUsage> searchBuilder = SearchBuilder.builder();

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
        // state.setOnDemand(onDemand);
        state.setRunning(running);
        // state.setSpot(spot);
        state.setTerminated(terminated);
        state.setStopped(stopped);

        return state;
    }

}
