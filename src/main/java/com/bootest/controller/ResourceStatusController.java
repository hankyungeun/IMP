//package com.bootest.controller;
//
//import com.bootest.dto.GetMonthlyUsageDto;
//import com.bootest.model.ResourceUsage;
//import com.bootest.repository.ResourceUsageRepo;
//import com.bootest.searcher.SearchBuilder;
//import com.bootest.searcher.SearchOperationType;
//import com.bootest.service.ResourceUsageService;
//import com.bootest.type.UsageDataType;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/status")
//
//public class ResourceStatusController {
//    private final ResourceUsageService service;
//    private final ResourceUsageRepo resourceUsageRepo;
//
//    @GetMapping
//    public List<GetMonthlyUsageDto> findAllByMonthAvg(
//            @RequestParam(required = false) String resourceState) throws JsonMappingException, JsonProcessingException {
//        SearchBuilder<ResourceUsage> searchBuilder = SearchBuilder.builder();
//
//        if (resourceState != null) {
//            searchBuilder.with("resourceState", SearchOperationType.EQUAL, resourceState);
//        }
//
//        List<ResourceUsage> usages = resourceUsageRepo.findAll(searchBuilder.build());
//
//        return service.findAllByMonthAvg(resourceState, usages);
//    }
//
//}
