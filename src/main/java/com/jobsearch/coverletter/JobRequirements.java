package com.jobsearch.coverletter;

import java.util.ArrayList;
import java.util.List;

public class JobRequirements {
    private String jobTitle;
    private List<String> technicalSkills = new ArrayList<>();
    private List<String> softSkills = new ArrayList<>();
    private int yearsExperience;
    private String educationLevel;
    private List<String> responsibilities = new ArrayList<>();
    private List<String> qualifications = new ArrayList<>();
    private List<String> keyPhrases = new ArrayList<>();
    
    // Getters and setters
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public List<String> getTechnicalSkills() { return technicalSkills; }
    public void setTechnicalSkills(List<String> skills) { this.technicalSkills = skills; }
    
    public List<String> getSoftSkills() { return softSkills; }
    public void setSoftSkills(List<String> skills) { this.softSkills = skills; }
    
    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int years) { this.yearsExperience = years; }
    
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String level) { this.educationLevel = level; }
    
    public List<String> getResponsibilities() { return responsibilities; }
    public void setResponsibilities(List<String> resp) { this.responsibilities = resp; }
    
    public List<String> getQualifications() { return qualifications; }
    public void setQualifications(List<String> qual) { this.qualifications = qual; }
    
    public List<String> getKeyPhrases() { return keyPhrases; }
    public void setKeyPhrases(List<String> phrases) { this.keyPhrases = phrases; }
}