package com.bootest.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootest.dto.MetricStatisticDto;
import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.task.InstanceTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TaskTesterController {

    private final InstanceTask task;
    private final ResourceUsageRepo resourceUsageRepo;
    private final ObjectMapper mapper;

    @GetMapping
    public void runTest() throws JsonProcessingException {
        task.updateResourceUsage(List.of(LocalDate.of(2022, 8, 2)));
    }

    @GetMapping("/data")
    public List<StatisticDataDto> findAll() throws JsonMappingException, JsonProcessingException {

        List<StatisticDataDto> results = new ArrayList<>();
        List<ResourceUsage> usages = resourceUsageRepo.findAll();

        for (ResourceUsage ru : usages) {

            StatisticDataDto metric = mapper.readValue(ru.getAverage(), StatisticDataDto.class);
            results.add(metric);

            for (StatisticDataValueDto data : metric.getData()) {

            }
        }
        return results;
    }
}
