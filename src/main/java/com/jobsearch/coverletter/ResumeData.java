package com.jobsearch.coverletter;

import java.util.*;

/**
 * Represents parsed data from a user's resume.
 * Contains all extracted information including contact details, skills, experience, and education.
 */
public class ResumeData {
    private String name;
    private String email;
    private String phone;
    private String linkedIn;
    private Map<String, Integer> skills; // skill name -> years of experience
    private List<Map<String, String>> experience; // List of job experiences
    private List<Map<String, String>> education; // List of educational achievements
    private List<String> projects; // Personal/professional projects
    private int totalYearsExperience;
    private List<String> certifications;
    private String summary;
    private List<String> languages;
    
    // Constructor
    public ResumeData() {
        this.skills = new HashMap<>();
        this.experience = new ArrayList<>();
        this.education = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.certifications = new ArrayList<>();
        this.languages = new ArrayList<>();
        this.totalYearsExperience = 0;
    }
    
    // Contact Information Getters/Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getLinkedIn() {
        return linkedIn;
    }
    
    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    // Skills Management
    /**
     * Add a skill with years of experience
     * @param skill The skill name
     * @param yearsExperience Years of experience with this skill
     */
    public void addSkill(String skill, int yearsExperience) {
        this.skills.put(skill.toLowerCase(), yearsExperience);
    }
    
    /**
     * Check if resume contains a specific skill
     * @param skill The skill to check
     * @return true if skill exists in resume
     */
    public boolean hasSkill(String skill) {
        return skills.containsKey(skill.toLowerCase());
    }
    
    /**
     * Get years of experience for a specific skill
     * @param skill The skill name
     * @return Years of experience, or 0 if not found
     */
    public int getSkillExperience(String skill) {
        return skills.getOrDefault(skill.toLowerCase(), 0);
    }
    
    public Map<String, Integer> getSkills() {
        return new HashMap<>(skills);
    }
    
    public void setSkills(Map<String, Integer> skills) {
        this.skills = skills != null ? skills : new HashMap<>();
    }
    
    /**
     * Get all skills as a formatted string
     * @return Comma-separated list of skills
     */
    public String getSkillsAsString() {
        return String.join(", ", skills.keySet());
    }
    
    // Experience Management
    /**
     * Add a job experience
     * @param jobTitle Job title
     * @param company Company name
     * @param startYear Start year
     * @param endYear End year (or "Present")
     * @param description Job description/responsibilities
     */
    public void addExperience(String jobTitle, String company, 
                             String startYear, String endYear, String description) {
        Map<String, String> job = new HashMap<>();
        job.put("title", jobTitle);
        job.put("company", company);
        job.put("startYear", startYear);
        job.put("endYear", endYear);
        job.put("description", description);
        experience.add(job);
    }
    
    public List<Map<String, String>> getExperience() {
        return new ArrayList<>(experience);
    }
    
    public void setExperience(List<Map<String, String>> experience) {
        this.experience = experience != null ? experience : new ArrayList<>();
    }
    
    /**
     * Get the most recent job
     * @return Most recent job map, or null if no experience
     */
    public Map<String, String> getMostRecentJob() {
        if (experience.isEmpty()) {
            return null;
        }
        return experience.get(0);
    }
    
    /**
     * Get all job titles as a list
     * @return List of job titles
     */
    public List<String> getJobTitles() {
        List<String> titles = new ArrayList<>();
        for (Map<String, String> job : experience) {
            if (job.containsKey("title")) {
                titles.add(job.get("title"));
            }
        }
        return titles;
    }
    
    // Education Management
    /**
     * Add an education entry
     * @param degree Degree level (e.g., Bachelor's, Master's, PhD)
     * @param field Field of study
     * @param school School/University name
     * @param year Graduation year
     */
    public void addEducation(String degree, String field, String school, String year) {
        Map<String, String> edu = new HashMap<>();
        edu.put("degree", degree);
        edu.put("field", field);
        edu.put("school", school);
        edu.put("year", year);
        education.add(edu);
    }
    
    public List<Map<String, String>> getEducation() {
        return new ArrayList<>(education);
    }
    
    public void setEducation(List<Map<String, String>> education) {
        this.education = education != null ? education : new ArrayList<>();
    }
    
    /**
     * Check if resume meets education requirements
     * @param requiredLevel Required education level
     * @return true if resume meets requirement
     */
    public boolean meetsEducationRequirement(String requiredLevel) {
        if (requiredLevel == null || requiredLevel.isEmpty()) {
            return true;
        }
        
        String required = requiredLevel.toLowerCase();
        
        // Map degree levels to hierarchy
        Map<String, Integer> degreeHierarchy = new HashMap<>();
        degreeHierarchy.put("high school", 1);
        degreeHierarchy.put("associate", 2);
        degreeHierarchy.put("bachelor", 3);
        degreeHierarchy.put("master", 4);
        degreeHierarchy.put("phd", 5);
        
        int requiredLevel_int = degreeHierarchy.getOrDefault(required, 0);
        
        for (Map<String, String> edu : education) {
            String degree = edu.get("degree").toLowerCase();
            for (Map.Entry<String, Integer> entry : degreeHierarchy.entrySet()) {
                if (degree.contains(entry.getKey()) && entry.getValue() >= requiredLevel_int) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get highest education level
     * @return Highest degree or null
     */
    public String getHighestDegree() {
        if (education.isEmpty()) {
            return null;
        }
        
        // Simple logic: return first degree (assuming sorted)
        Map<String, String> firstEdu = education.get(0);
        return firstEdu.get("degree");
    }
    
    // Projects Management
    public void addProject(String projectName) {
        projects.add(projectName);
    }
    
    public List<String> getProjects() {
        return new ArrayList<>(projects);
    }
    
    public void setProjects(List<String> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
    }
    
    // Certifications Management
    public void addCertification(String certification) {
        certifications.add(certification);
    }
    
    public List<String> getCertifications() {
        return new ArrayList<>(certifications);
    }
    
    public void setCertifications(List<String> certifications) {
        this.certifications = certifications != null ? certifications : new ArrayList<>();
    }
    
    // Languages Management
    public void addLanguage(String language) {
        languages.add(language);
    }
    
    public List<String> getLanguages() {
        return new ArrayList<>(languages);
    }
    
    public void setLanguages(List<String> languages) {
        this.languages = languages != null ? languages : new ArrayList<>();
    }
    
    // Experience Summary
    public int getTotalYearsExperience() {
        return totalYearsExperience;
    }
    
    public void setTotalYearsExperience(int years) {
        this.totalYearsExperience = Math.max(0, years);
    }
    
    // Soft Skills - extracted from descriptions
    private List<String> softSkills = new ArrayList<>();
    
    public void addSoftSkill(String skill) {
        if (!softSkills.contains(skill.toLowerCase())) {
            softSkills.add(skill.toLowerCase());
        }
    }
    
    public List<String> getSoftSkills() {
        return new ArrayList<>(softSkills);
    }
    
    public boolean hasSoftSkill(String skill) {
        return softSkills.stream()
            .anyMatch(s -> s.equalsIgnoreCase(skill));
    }
    
    // Utility Methods
    /**
     * Get a summary of the resume
     * @return Formatted string with key resume information
     */
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        
        if (name != null) {
            summary.append("Name: ").append(name).append("\n");
        }
        
        summary.append("Experience: ").append(totalYearsExperience).append(" years\n");
        summary.append("Skills: ").append(skills.size()).append("\n");
        
        if (!education.isEmpty()) {
            Map<String, String> latestEdu = education.get(0);
            summary.append("Education: ").append(latestEdu.get("degree")).append("\n");
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "ResumeData{" +
                "name='" + name + '\'' +
                ", totalYearsExperience=" + totalYearsExperience +
                ", skills=" + skills.size() +
                ", experience=" + experience.size() +
                ", education=" + education.size() +
                ", projects=" + projects.size() +
                '}';
    }
}