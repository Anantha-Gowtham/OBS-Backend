package com.obs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Frontend frontend = new Frontend();

    public Frontend getFrontend() { return frontend; }
    public void setFrontend(Frontend frontend) { this.frontend = frontend; }

    public static class Frontend {
        private String url;
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
