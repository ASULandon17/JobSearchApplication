package com.jobsearch.model;

public class SearchFilters {
    private String searchTerms;
    private WorkModel workModel;
    private String city;
    private String state;
    private ExperienceLevel experienceLevel;
    
    public enum WorkModel {
        REMOTE("Remote"),
        HYBRID("Hybrid"),
        IN_PERSON("In-Person"),
        NO_PREFERENCE("No Preference");
        
        private final String displayName;
        
        WorkModel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum ExperienceLevel {
        JUNIOR("Junior"),
        MID_LEVEL("Mid-Level"),
        SENIOR("Senior"),
        NO_PREFERENCE("No Preference");
        
        private final String displayName;
        
        ExperienceLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public SearchFilters() {
        this.workModel = WorkModel.NO_PREFERENCE;
        this.experienceLevel = ExperienceLevel.NO_PREFERENCE;
    }
    
    public String getSearchTerms() { return searchTerms; }
    public void setSearchTerms(String searchTerms) { this.searchTerms = searchTerms; }
    
    public WorkModel getWorkModel() { return workModel; }
    public void setWorkModel(WorkModel workModel) { this.workModel = workModel; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { 
        this.experienceLevel = experienceLevel; 
    }
    
    public boolean hasLocationFilter() {
        return city != null && !city.trim().isEmpty() && 
               state != null && !state.trim().isEmpty();
    }
    
    public String getLocationString() {
        if (hasLocationFilter()) {
            return city + ", " + state;
        }
        return "";
    }
}