package com.market.main.emarket.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserActionLogger {

    private static final Logger logger = LoggerFactory.getLogger("USER_ACTION_LOGGER");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void logAction(String userId, String productId, String action) {
        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("user", userId);
            logEntry.put("product", productId);
            logEntry.put("action", action);
            logEntry.put("timestamp", Instant.now().toString());

            String json = objectMapper.writeValueAsString(logEntry);
            logger.info(json); // écrit dans user_actions.log
        } catch (Exception e) {
            e.printStackTrace(); // en cas d’erreur de sérialisation
        }
    }
}
