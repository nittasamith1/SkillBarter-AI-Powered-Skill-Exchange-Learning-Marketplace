package com.skillbarter.reputation.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.trust.weights")
public class TrustScoreConfig {

    private double rating = 0.40;
    private double completion = 0.20;
    private double reliability = 0.20;
    private double response = 0.10;
    private double cancellation = 0.10;

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getCompletion() { return completion; }
    public void setCompletion(double completion) { this.completion = completion; }

    public double getReliability() { return reliability; }
    public void setReliability(double reliability) { this.reliability = reliability; }

    public double getResponse() { return response; }
    public void setResponse(double response) { this.response = response; }

    public double getCancellation() { return cancellation; }
    public void setCancellation(double cancellation) { this.cancellation = cancellation; }
}
