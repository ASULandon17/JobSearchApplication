package com.jobsearch.model;

import java.time.LocalDate;

public class JobPosting {
    private String title;
    private String company;
    private String location;
    private String salary;
    private LocalDate postedDate;
    private String url;
    private String description;
    private int relevanceScore;
    private int reputabilityScore;
    private String source;
    
    public JobPosting() {
        this.relevanceScore = 0;
        this.reputabilityScore = 0;
    }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    
    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(int relevanceScore) { 
        this.relevanceScore = Math.min(10, Math.max(0, relevanceScore)); 
    }
    
    public int getReputabilityScore() { return reputabilityScore; }
    public void setReputabilityScore(int reputabilityScore) { 
        this.reputabilityScore = Math.min(10, Math.max(0, reputabilityScore)); 
    }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}