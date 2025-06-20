package com.footballdynasty.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class SentryConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SentryConfig.class);
    
    @Value("${sentry.dsn:}")
    private String sentryDsn;
    
    @Value("${sentry.environment:development}")
    private String environment;
    
    @Value("${sentry.debug:false}")
    private boolean debug;
    
    @PostConstruct
    public void init() {
        logger.info("SENTRY_CONFIG: Manual Sentry initialization starting");
        logger.info("SENTRY_DSN_CHECK: DSN={}", sentryDsn != null && !sentryDsn.isEmpty() ? "CONFIGURED" : "NOT_CONFIGURED");
        logger.info("SENTRY_ENV: {}", environment);
        logger.info("SENTRY_DEBUG: {}", debug);
        
        if (sentryDsn != null && !sentryDsn.isEmpty()) {
            try {
                // Initialize Sentry manually to ensure proper configuration
                Sentry.init(options -> {
                    options.setDsn(sentryDsn);
                    options.setEnvironment(environment);
                    options.setDebug(debug);
                    options.setTracesSampleRate(1.0);
                    options.setSendDefaultPii(true);
                    options.setMaxBreadcrumbs(100);
                    
                    logger.info("SENTRY_MANUAL_INIT: Manually initializing Sentry with DSN");
                });
                
                boolean enabled = Sentry.isEnabled();
                logger.info("SENTRY_MANUAL_ENABLED: {}", enabled);
                
                if (enabled) {
                    // Test capture
                    String testEventId = Sentry.captureMessage("Sentry initialization test").toString();
                    logger.info("SENTRY_TEST_EVENT: {}", testEventId);
                }
                
            } catch (Exception e) {
                logger.error("SENTRY_INIT_ERROR: Failed to initialize Sentry manually", e);
            }
        } else {
            logger.warn("SENTRY_WARNING: No DSN configured, skipping manual initialization");
        }
    }
}