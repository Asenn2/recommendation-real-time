package com.market.main.emarket.controller;


import com.market.main.emarket.services.HBaseThriftService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hbase")
public class HBaseThriftController {

    private final HBaseThriftService service;

    public HBaseThriftController(HBaseThriftService service) {
        this.service = service;
    }

    @GetMapping("/recommendations/{userId}")
    public Map<String, String> getRecommendations(@PathVariable String userId) throws Exception {
        return service.getRecommendation(userId);
    }
}

