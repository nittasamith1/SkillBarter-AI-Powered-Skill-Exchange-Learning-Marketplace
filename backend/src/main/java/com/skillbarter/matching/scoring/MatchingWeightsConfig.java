package com.skillbarter.matching.scoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.matching.weights")
public class MatchingWeightsConfig {

    private double skillCompatibility = 0.30;
    private double goalCompatibility = 0.20;
    private double availability = 0.20;
    private double proficiency = 0.10;
    private double language = 0.10;
    private double trust = 0.10;

    public double getSkillCompatibility() { return skillCompatibility; }
    public void setSkillCompatibility(double skillCompatibility) { this.skillCompatibility = skillCompatibility; }

    public double getGoalCompatibility() { return goalCompatibility; }
    public void setGoalCompatibility(double goalCompatibility) { this.goalCompatibility = goalCompatibility; }

    public double getAvailability() { return availability; }
    public void setAvailability(double availability) { this.availability = availability; }

    public double getProficiency() { return proficiency; }
    public void setProficiency(double proficiency) { this.proficiency = proficiency; }

    public double getLanguage() { return language; }
    public void setLanguage(double language) { this.language = language; }

    public double getTrust() { return trust; }
    public void setTrust(double trust) { this.trust = trust; }
}
