package com.cinemaabyss.proxy;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {
    private String monolithUrl = "http://localhost:8080";
    private String moviesServiceUrl = "http://localhost:8081";
    private String eventsServiceUrl = "http://localhost:8082";
    private boolean gradualMigration = true;
    private int moviesMigrationPercent;

    public String getMonolithUrl() {
        return monolithUrl;
    }

    public void setMonolithUrl(String value) {
        monolithUrl = value;
    }

    public String getMoviesServiceUrl() {
        return moviesServiceUrl;
    }

    public void setMoviesServiceUrl(String value) {
        moviesServiceUrl = value;
    }

    public String getEventsServiceUrl() {
        return eventsServiceUrl;
    }

    public void setEventsServiceUrl(String value) {
        eventsServiceUrl = value;
    }

    public boolean isGradualMigration() {
        return gradualMigration;
    }

    public void setGradualMigration(boolean value) {
        gradualMigration = value;
    }

    public int getMoviesMigrationPercent() {
        return moviesMigrationPercent;
    }

    public void setMoviesMigrationPercent(int value) {
        moviesMigrationPercent = Math.max(0, Math.min(100, value));
    }
}
