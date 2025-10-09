package com.jobsearch.coverletter;

import java.util.*;

/**
 * Represents the result of matching job requirements with resume skills.
 * Contains matched and unmatched skills, experience level matching, and overall score.
 */
public class SkillMatchResult {
    private List<String> matchedSkills; // Technical skills that match
    private List<String> unmatchedSkills; // Required skills not in resume
    private List<String> matchedSoftSkills; // Soft skills that match
    private boolean experienceMatch; // Does resume experience level match requirement?
    private double overallMatchScore; // 0.0 to 1.0
    private Map<String, String> skillDescriptions; // Skill -> how candidate used it
    private List<String> transferableSkills; // Skills from other areas that apply
    private int yearsExperienceMatch; // How many years of required experience candidate has
    private int yearsExperienceRequired; // Years of experience required
    private double technicalScore; // Score for technical skills match (0.0-1.0)
    private double softSkillsScore; // Score for soft skills match (0.0-1.0)
    private double experienceScore; // Score for experience level match (0.0-1.0)
    
    // Constructor
    public SkillMatchResult() {
        this.matchedSkills = new ArrayList<>();
        this.unmatchedSkills = new ArrayList<>();
        this.matchedSoftSkills = new ArrayList<>();
        this.skillDescriptions = new HashMap<>();
        this.transferableSkills = new ArrayList<>();
        this.experienceMatch = false;
        this.overallMatchScore = 0.0;
        this.yearsExperienceMatch = 0;
        this.yearsExperienceRequired = 0;
        this.technicalScore = 0.0;
        this.softSkillsScore = 0.0;
        this.experienceScore = 0.0;
    }
    
    // Matched Skills Management
    /**
     * Add a matched technical skill
     * @param skill The skill that matched
     * @param yearsOfExperience Years the candidate has with this skill
     */
    public void addMatchedSkill(String skill, int yearsOfExperience) {
        if (skill != null && !skill.isEmpty()) {
            matchedSkills.add(skill.toLowerCase());
            skillDescriptions.put(skill.toLowerCase(), 
                String.format("%d years of experience", yearsOfExperience));
        }
    }
    
    public List<String> getMatchedSkills() {
        return new ArrayList<>(matchedSkills);
    }
    
    public void setMatchedSkills(List<String> skills) {
        this.matchedSkills = skills != null ? skills : new ArrayList<>();
    }
    
    /**
     * Get number of matched skills
     * @return Count of matched skills
     */
    public int getMatchedSkillsCount() {
        return matchedSkills.size();
    }
    
    /**
     * Get description of how candidate uses a skill
     * @param skill Skill name
     * @return Description, or null if not found
     */
    public String getSkillDescription(String skill) {
        return skillDescriptions.get(skill.toLowerCase());
    }
    
    // Unmatched Skills Management
    /**
     * Add a skill that's required but not found in resume
     * @param skill The required skill
     */
    public void addUnmatchedSkill(String skill) {
        if (skill != null && !skill.isEmpty()) {
            unmatchedSkills.add(skill.toLowerCase());
        }
    }
    
    public List<String> getUnmatchedSkills() {
        return new ArrayList<>(unmatchedSkills);
    }
    
    public void setUnmatchedSkills(List<String> skills) {
        this.unmatchedSkills = skills != null ? skills : new ArrayList<>();
    }
    
    /**
     * Get number of unmatched skills
     * @return Count of unmatched skills
     */
    public int getUnmatchedSkillsCount() {
        return unmatchedSkills.size();
    }
    
    // Soft Skills Management
    /**
     * Add a matched soft skill
     * @param skill The soft skill
     */
    public void addMatchedSoftSkill(String skill) {
        if (skill != null && !skill.isEmpty()) {
            matchedSoftSkills.add(skill.toLowerCase());
        }
    }
    
    public List<String> getMatchedSoftSkills() {
        return new ArrayList<>(matchedSoftSkills);
    }
    
    public void setMatchedSoftSkills(List<String> skills) {
        this.matchedSoftSkills = skills != null ? skills : new ArrayList<>();
    }
    
    /**
     * Get number of matched soft skills
     * @return Count of matched soft skills
     */
    public int getMatchedSoftSkillsCount() {
        return matchedSoftSkills.size();
    }
    
    // Transferable Skills
    /**
     * Add a skill from a different domain that applies to this job
     * @param skill Transferable skill
     */
    public void addTransferableSkill(String skill) {
        if (skill != null && !skill.isEmpty()) {
            transferableSkills.add(skill);
        }
    }
    
    public List<String> getTransferableSkills() {
        return new ArrayList<>(transferableSkills);
    }
    
    // Experience Matching
    /**
     * Set whether resume meets experience level requirements
     * @param matches true if experience level matches
     */
    public void setExperienceMatch(boolean matches) {
        this.experienceMatch = matches;
    }
    
    public boolean isExperienceMatch() {
        return experienceMatch;
    }
    
    /**
     * Set candidate's years of relevant experience
     * @param years Candidate's experience in this area
     */
    public void setYearsExperienceMatch(int years) {
        this.yearsExperienceMatch = Math.max(0, years);
    }
    
    public int getYearsExperienceMatch() {
        return yearsExperienceMatch;
    }
    
    /**
     * Set required years of experience
     * @param years Years required by the job
     */
    public void setYearsExperienceRequired(int years) {
        this.yearsExperienceRequired = Math.max(0, years);
    }
    
    public int getYearsExperienceRequired() {
        return yearsExperienceRequired;
    }
    
    // Overall Scoring
    /**
     * Set the overall match score
     * @param score Score from 0.0 (no match) to 1.0 (perfect match)
     */
    public void setOverallMatchScore(double score) {
        this.overallMatchScore = Math.max(0.0, Math.min(1.0, score));
    }
    
    public double getOverallMatchScore() {
        return overallMatchScore;
    }
    
    /**
     * Set individual component scores
     * @param technical Score for technical skills (0.0-1.0)
     * @param softSkills Score for soft skills (0.0-1.0)
     * @param experience Score for experience level (0.0-1.0)
     */
    public void setComponentScores(double technical, double softSkills, double experience) {
        this.technicalScore = Math.max(0.0, Math.min(1.0, technical));
        this.softSkillsScore = Math.max(0.0, Math.min(1.0, softSkills));
        this.experienceScore = Math.max(0.0, Math.min(1.0, experience));
        
        // Calculate overall score as weighted average
        this.overallMatchScore = (technical * 0.5) + (softSkills * 0.2) + (experience * 0.3);
    }
    
    public double getTechnicalScore() {
        return technicalScore;
    }
    
    public double getSoftSkillsScore() {
        return softSkillsScore;
    }
    
    public double getExperienceScore() {
        return experienceScore;
    }
    
    // Utility Methods
    /**
     * Get match percentage (0-100)
     * @return Percentage value
     */
    public int getMatchPercentage() {
        return (int) (overallMatchScore * 100);
    }
    
    /**
     * Get match rating (Poor, Fair, Good, Excellent)
     * @return String rating
     */
    public String getMatchRating() {
        if (overallMatchScore >= 0.8) {
            return "Excellent";
        } else if (overallMatchScore >= 0.6) {
            return "Good";
        } else if (overallMatchScore >= 0.4) {
            return "Fair";
        } else {
            return "Poor";
        }
    }
    
    /**
     * Get a detailed summary of the match
     * @return Formatted summary string
     */
    public String getMatchSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Match Summary:\n");
        summary.append("Overall: ").append(getMatchRating())
            .append(" (").append(getMatchPercentage()).append("%)\n");
        
        summary.append("Matched Skills: ").append(matchedSkills.size()).append("\n");
        summary.append("Unmatched Skills: ").append(unmatchedSkills.size()).append("\n");
        
        summary.append("Soft Skills: ").append(matchedSoftSkills.size()).append("\n");
        
        if (yearsExperienceMatch >= yearsExperienceRequired) {
            summary.append("Experience: ✓ Meets requirements\n");
        } else {
            summary.append("Experience: ").append(yearsExperienceMatch)
                .append("/").append(yearsExperienceRequired).append(" years\n");
        }
        
        if (!transferableSkills.isEmpty()) {
            summary.append("Transferable Skills: ").append(transferableSkills.size()).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Get recommendations for covering skill gaps
     * @return List of recommendations
     */
    public List<String> getRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        if (!unmatchedSkills.isEmpty() && unmatchedSkills.size() <= 3) {
            recommendations.add("Highlight transferable skills that could apply to: " + 
                String.join(", ", unmatchedSkills));
        }
        
        if (yearsExperienceMatch < yearsExperienceRequired) {
            recommendations.add("Emphasize proven ability to learn quickly and take on new challenges");
        }
        
        if (matchedSkills.size() >= 5) {
            recommendations.add("Strong technical foundation - lead with relevant projects");
        }
        
        if (matchedSoftSkills.size() >= 3) {
            recommendations.add("Strong soft skills - emphasize leadership and collaboration");
        }
        
        if (overallMatchScore < 0.5) {
            recommendations.add("Focus on transferable skills and willingness to grow");
        }
        
        return recommendations;
    }
    
    @Override
    public String toString() {
        return "SkillMatchResult{" +
                "matchedSkills=" + matchedSkills.size() +
                ", unmatchedSkills=" + unmatchedSkills.size() +
                ", matchedSoftSkills=" + matchedSoftSkills.size() +
                ", experienceMatch=" + experienceMatch +
                ", overallMatchScore=" + String.format("%.2f", overallMatchScore) +
                ", rating='" + getMatchRating() + '\'' +
                '}';
    }
}