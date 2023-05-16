package com.bootest.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bootest.dto.optimizer.GetResourceOptResponse;
import com.bootest.dto.optimizer.GetRightSizeOptResponse;
import com.bootest.dto.optimizer.OptimizationRequestDataDto;
import com.bootest.dto.optimizer.OptimizationRequestFormDto;
import com.bootest.dto.optimizer.RightSizeThresholdRequest;
import com.bootest.model.Account;
import com.bootest.model.ResultObject;
import com.bootest.repository.AccountRepo;
import com.bootest.service.OptimizationService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/optimizer")
public class VrmOptimizerController {

    private final OptimizationService service;
    private final AccountRepo accountRepo;

    @GetMapping
    //사용하지 않는 인스턴스, 버전 관리가 필요한 인스턴스 조회
    public ResponseEntity<ResultObject> findAllOptimizable(@RequestParam String accountId,
            @RequestParam String regionId) throws InterruptedException, IOException, ExecutionException {
        ResultObject result = new ResultObject();

        List<GetResourceOptResponse> results = new ArrayList<>();

        Account account = accountRepo.findByAccountId(accountId).orElse(null);

        if (account == null) {
            result.setResult(false);
            result.setMessage("Invalid account id: " + accountId);
            return new ResponseEntity<ResultObject>(result, HttpStatus.UNAUTHORIZED);
        }

        results.add(service.findOptimizable(account, regionId));
        result.setResult(true);
        result.setData(results);

        return new ResponseEntity<ResultObject>(result, HttpStatus.OK);
    }

    @PostMapping("/right-size")
    public ResponseEntity<ResultObject> findRightSizeOptimizable(
            @RequestParam String accountId,
            @RequestParam String regionId,
            // 비교일
            @RequestParam(required = false) Integer days,
            @RequestBody RightSizeThresholdRequest request)
            throws InterruptedException, ExecutionException, IOException {
        ResultObject result = new ResultObject();

        List<GetRightSizeOptResponse> results = new ArrayList<>();

        Account credential = accountRepo.findByAccountId(accountId).orElse(null);

        if (credential == null) {
            result.setResult(false);
            result.setMessage("Invalid account id: " + accountId);
            return new ResponseEntity<ResultObject>(result, HttpStatus.UNAUTHORIZED);
        }

        Integer accumulatedDays = 30;
        if (days != null) {
            accumulatedDays = days;
        }

        results.add(service.findRightSizable(credential, regionId, request, accumulatedDays));

        result.setResult(true);
        result.setData(results);

        return new ResponseEntity<ResultObject>(result, HttpStatus.OK);
    }

    @PostMapping
    // 요청시 추천 사항대로 적용
    public ResponseEntity<ResultObject> optimize(@RequestBody OptimizationRequestFormDto temp)
            throws JsonParseException, JsonMappingException, IOException, InterruptedException {
        ResultObject result = new ResultObject();
        List<OptimizationRequestDataDto> failedResults = new ArrayList<>();

        Account credential = accountRepo.findByAccountId(temp.getAccountId()).orElse(null);

        if (credential == null) {
            result.setResult(false);
            result.setMessage("Invalid account id: " + temp.getAccountId());
            return new ResponseEntity<ResultObject>(result, HttpStatus.UNAUTHORIZED);
        }

        for (OptimizationRequestDataDto optData : temp.getData()) {
            try {
                CompletableFuture<Boolean> optimizedResult = service.optimizer(optData, credential);
                if (!optimizedResult.get()) {
                    failedResults.add(optData);
                }
            } catch (Exception e) {
                log.error("Optimization request failed. message: {}", e.getMessage(), e);
            }
        }

        Integer succeeded = temp.getData().size() - failedResults.size();

        result.setResult(true);
        result.setData(failedResults);
        result.setMessage("Optimization complete! Succeeded: " + succeeded + " Failed: " + failedResults.size());
        return new ResponseEntity<ResultObject>(result, HttpStatus.OK);
    }
}
