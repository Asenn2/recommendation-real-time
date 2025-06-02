package com.market.main.emarket.services;


import com.market.main.emarket.config.HBaseThriftClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HBaseThriftService {

    private final HBaseThriftClient client;

    public HBaseThriftService() {
        // ⚠️ Adapter selon l'environnement Docker/hôte
        this.client = new HBaseThriftClient("localhost", 9090);
    }

    public Map<String, String> getRecommendation(String userId) throws Exception {
        return client.getUserRecommendation(userId);
    }
}

