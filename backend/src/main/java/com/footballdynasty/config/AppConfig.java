package com.footballdynasty.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    @Autowired
    private Environment environment;
    
    private String env = "development";
    private MockData mockData = new MockData();
    
    public String getEnvironment() {
        return env;
    }
    
    public void setEnvironment(String environment) {
        this.env = environment;
    }
    
    public MockData getMockData() {
        return mockData;
    }
    
    public void setMockData(MockData mockData) {
        this.mockData = mockData;
    }
    
    public boolean isTestingEnvironment() {
        return environment.acceptsProfiles("testing");
    }
    
    public boolean isProductionEnvironment() {
        return environment.acceptsProfiles("production");
    }
    
    public boolean isDevelopmentEnvironment() {
        return environment.acceptsProfiles("development");
    }
    
    public boolean isMockDataEnabled() {
        return mockData.isEnabled() || isTestingEnvironment();
    }
    
    public static class MockData {
        private boolean enabled = false;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}