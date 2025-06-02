package com.market.main.emarket.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.main.emarket.config.HBaseThriftClient;
import com.market.main.emarket.model.Product;
import com.market.main.emarket.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class RecommendationService {

    @Autowired
    private HBaseThriftClient thriftClient;

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getRecommendedProductsIds(String userId) throws Exception {
        List<Long>prodIds=thriftClient.getUserRecommendation(userId);
        return productRepository.findAllById(prodIds);
    }
}
