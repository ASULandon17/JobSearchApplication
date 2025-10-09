package com.jobsearch.coverletter;

import java.util.*;

/**
 * Contains information about the company posting the job.
 * Includes company details, industry, size, values, and culture indicators.
 */
public class CompanyInfo {
    private String name;
    private String description;
    private String industry;
    private String size; // "Startup", "Small", "Mid-size", "Large", "Enterprise"
    private String website;
    private List<String> values; // Company values and mission
    private List<String> recentNews; // Recent company achievements/news
    private String founded;
    private String headquarters;
    private List<String> productServices; // What the company does
    private String culture; // Company culture description
    private String techStack; // Technologies the company uses
    private List<String> keywords; // Keywords associated with the company
    private double matchScore; // How well resume matches company
    
    // Constructor
    public CompanyInfo() {
        this.values = new ArrayList<>();
        this.recentNews = new ArrayList<>();
        this.productServices = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.matchScore = 0.0;
    }
    
    // Basic Information Getters/Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIndustry() {
        return industry != null ? industry : "Technology";
    }
    
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    
    public String getSize() {
        return size != null ? size : "Mid-size";
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public String getFounded() {
        return founded;
    }
    
    public void setFounded(String founded) {
        this.founded = founded;
    }
    
    public String getHeadquarters() {
        return headquarters;
    }
    
    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }
    
    // Company Culture and Values
    public List<String> getValues() {
        return new ArrayList<>(values);
    }
    
    public void setValues(List<String> values) {
        this.values = values != null ? values : new ArrayList<>();
    }
    
    /**
     * Add a company value
     * @param value Company value/mission statement
     */
    public void addValue(String value) {
        if (value != null && !value.isEmpty()) {
            values.add(value);
        }
    }
    
    /**
     * Get values as a formatted string
     * @return Comma-separated values
     */
    public String getValuesAsString() {
        return String.join(", ", values);
    }
    
    public String getCulture() {
        return culture;
    }
    
    public void setCulture(String culture) {
        this.culture = culture;
    }
    
    // Products and Services
    public List<String> getProductServices() {
        return new ArrayList<>(productServices);
    }
    
    public void setProductServices(List<String> products) {
        this.productServices = products != null ? products : new ArrayList<>();
    }
    
    public void addProductService(String product) {
        if (product != null && !product.isEmpty()) {
            productServices.add(product);
        }
    }
    
    public String getProductServicesAsString() {
        return String.join(", ", productServices);
    }
    
    // Technology Stack
    public String getTechStack() {
        return techStack;
    }
    
    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }
    
    /**
     * Get tech stack as a list
     * @return List of technologies
     */
    public List<String> getTechStackAsList() {
        if (techStack == null || techStack.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> techs = new ArrayList<>();
        String[] parts = techStack.split("[,;]");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                techs.add(trimmed);
            }
        }
        return techs;
    }
    
    // Recent News and Achievements
    public List<String> getRecentNews() {
        return new ArrayList<>(recentNews);
    }
    
    public void setRecentNews(List<String> news) {
        this.recentNews = news != null ? news : new ArrayList<>();
    }
    
    public void addRecentNews(String news) {
        if (news != null && !news.isEmpty()) {
            recentNews.add(news);
        }
    }
    
    // Keywords
    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }
    
    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            keywords.add(keyword.toLowerCase());
        }
    }
    
    // Match Score
    /**
     * Get how well the candidate's resume matches the company
     * @return Score between 0.0 and 1.0
     */
    public double getMatchScore() {
        return matchScore;
    }
    
    public void setMatchScore(double score) {
        this.matchScore = Math.max(0.0, Math.min(1.0, score));
    }
    
    // Utility Methods
    /**
     * Generate a compelling reason to work for this company
     * @return String highlighting why someone should want to work there
     */
    public String getCompellingReason() {
        StringBuilder reason = new StringBuilder();
        
        if (values != null && !values.isEmpty()) {
            reason.append(name).append(" is committed to ")
                .append(String.join(" and ", values));
        } else {
            reason.append(name).append(" is an innovative leader in the ")
                .append(industry).append(" industry");
        }
        
        if (description != null && !description.isEmpty()) {
            reason.append(". ").append(description);
        }
        
        return reason.toString();
    }
    
    /**
     * Get company description for cover letter
     * @return Formatted description
     */
    public String getDescriptionForCoverLetter() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        StringBuilder desc = new StringBuilder();
        desc.append(name).append(" is a ")
            .append(getSize().toLowerCase()).append(" company in the ")
            .append(getIndustry()).append(" industry");
        
        if (culture != null && !culture.isEmpty()) {
            desc.append(" with a focus on ").append(culture);
        }
        
        return desc.toString();
    }
    
    /**
     * Check if company values align with common values
     * @param value Value to check
     * @return true if value is mentioned
     */
    public boolean hasValue(String value) {
        return values.stream()
            .anyMatch(v -> v.toLowerCase().contains(value.toLowerCase()));
    }
    
    /**
     * Get a summary of the company
     * @return Formatted company summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Company: ").append(name).append("\n");
        summary.append("Industry: ").append(getIndustry()).append("\n");
        summary.append("Size: ").append(getSize()).append("\n");
        
        if (!values.isEmpty()) {
            summary.append("Values: ").append(String.join(", ", values)).append("\n");
        }
        
        if (techStack != null && !techStack.isEmpty()) {
            summary.append("Tech Stack: ").append(techStack).append("\n");
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "CompanyInfo{" +
                "name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                ", size='" + size + '\'' +
                ", values=" + values +
                ", matchScore=" + matchScore +
                '}';
    }
}