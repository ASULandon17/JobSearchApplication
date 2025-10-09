package com.jobsearch.coverletter;

import java.util.*;

/**
 * Contains the individual components of a cover letter.
 * Keeps paragraphs, achievements, and other content separately for flexible formatting.
 */
public class CoverLetterContent {
    private String opening; // Opening paragraph that hooks the reader
    private List<String> bodyParagraphs; // Main content paragraphs
    private String closing; // Closing paragraph with call to action
    private List<String> achievements; // Key achievements to highlight
    private String callToAction; // Specific next steps
    private Map<String, String> customSections; // Any additional custom sections
    private double confidenceScore; // How confident the system is in this letter (0.0-1.0)
    private List<String> highlights; // Key points to emphasize
    private String tone; // "Professional", "Enthusiastic", "Balanced"
    
    // Constructor
    public CoverLetterContent() {
        this.bodyParagraphs = new ArrayList<>();
        this.achievements = new ArrayList<>();
        this.customSections = new HashMap<>();
        this.highlights = new ArrayList<>();
        this.confidenceScore = 0.0;
        this.tone = "Professional";
    }
    
    // Opening Paragraph
    /**
     * Set the opening paragraph that introduces the candidate
     * @param opening Opening paragraph text
     */
    public void setOpening(String opening) {
        this.opening = opening;
    }
    
    /**
     * Get the opening paragraph
     * @return Opening text
     */
    public String getOpening() {
        if (opening == null || opening.isEmpty()) {
            return "I am writing to express my strong interest in this position.";
        }
        return opening;
    }
    
    // Body Paragraphs
    /**
     * Add a body paragraph
     * @param paragraph Paragraph content
     */
    public void addBodyParagraph(String paragraph) {
        if (paragraph != null && !paragraph.isEmpty()) {
            bodyParagraphs.add(paragraph);
        }
    }
    
    /**
     * Add multiple body paragraphs at once
     * @param paragraphs List of paragraphs
     */
    public void setBodyParagraphs(List<String> paragraphs) {
        this.bodyParagraphs = paragraphs != null ? new ArrayList<>(paragraphs) : new ArrayList<>();
    }
    
    /**
     * Get all body paragraphs
     * @return List of body paragraphs
     */
    public List<String> getBodyParagraphs() {
        return new ArrayList<>(bodyParagraphs);
    }
    
    /**
     * Get a specific body paragraph
     * @param index Index of the paragraph
     * @return Paragraph text, or null if index is out of range
     */
    public String getBodyParagraph(int index) {
        if (index >= 0 && index < bodyParagraphs.size()) {
            return bodyParagraphs.get(index);
        }
        return null;
    }
    
    /**
     * Get number of body paragraphs
     * @return Number of paragraphs
     */
    public int getBodyParagraphCount() {
        return bodyParagraphs.size();
    }
    
    // Closing Paragraph
    /**
     * Set the closing paragraph with call to action
     * @param closing Closing paragraph text
     */
    public void setClosing(String closing) {
        this.closing = closing;
    }
    
    /**
     * Get the closing paragraph
     * @return Closing text
     */
    public String getClosing() {
        if (closing == null || closing.isEmpty()) {
            return "Thank you for considering my application. " +
                   "I look forward to discussing how I can contribute to your team.";
        }
        return closing;
    }
    
    // Achievements/Highlights
    /**
     * Add an achievement or key point
     * @param achievement Achievement text
     */
    public void addAchievement(String achievement) {
        if (achievement != null && !achievement.isEmpty()) {
            achievements.add(achievement);
        }
    }
    
    /**
     * Add multiple achievements
     * @param achievements List of achievements
     */
    public void setAchievements(List<String> achievements) {
        this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
    }
    
    /**
     * Get all achievements
     * @return List of achievements
     */
    public List<String> getAchievements() {
        return new ArrayList<>(achievements);
    }
    
    /**
     * Get achievements as a bulleted string
     * @return Formatted bullet points
     */
    public String getAchievementsAsString() {
        StringBuilder sb = new StringBuilder();
        for (String achievement : achievements) {
            sb.append("• ").append(achievement).append("\n");
        }
        return sb.toString();
    }
    
    // Call to Action
    /**
     * Set the call to action
     * @param cta Call to action text
     */
    public void setCallToAction(String cta) {
        this.callToAction = cta;
    }
    
    /**
     * Get the call to action
     * @return CTA text
     */
    public String getCallToAction() {
        if (callToAction == null || callToAction.isEmpty()) {
            return "I would welcome the opportunity to discuss this further.";
        }
        return callToAction;
    }
    
    // Highlights
    /**
     * Add a key highlight/talking point
     * @param highlight Highlight text
     */
    public void addHighlight(String highlight) {
        if (highlight != null && !highlight.isEmpty()) {
            highlights.add(highlight);
        }
    }
    
    /**
     * Get all highlights
     * @return List of highlights
     */
    public List<String> getHighlights() {
        return new ArrayList<>(highlights);
    }
    
    /**
     * Set tone of the cover letter
     * @param tone "Professional", "Enthusiastic", or "Balanced"
     */
    public void setTone(String tone) {
        if (tone != null && 
            (tone.equals("Professional") || tone.equals("Enthusiastic") || tone.equals("Balanced"))) {
            this.tone = tone;
        }
    }
    
    public String getTone() {
        return tone;
    }
    
    // Custom Sections
    /**
     * Add a custom section (e.g., "Technical Skills", "Project Experience")
     * @param sectionName Name of the section
     * @param content Content of the section
     */
    public void addCustomSection(String sectionName, String content) {
        if (sectionName != null && !sectionName.isEmpty() && content != null) {
            customSections.put(sectionName, content);
        }
    }
    
    /**
     * Get a custom section
     * @param sectionName Name of the section
     * @return Section content, or null if not found
     */
    public String getCustomSection(String sectionName) {
        return customSections.get(sectionName);
    }
    
    /**
     * Get all custom sections
     * @return Map of section names to content
     */
    public Map<String, String> getCustomSections() {
        return new HashMap<>(customSections);
    }
    
    // Confidence Score
    /**
     * Set the confidence score for this cover letter
     * @param score Score from 0.0 to 1.0
     */
    public void setConfidenceScore(double score) {
        this.confidenceScore = Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Get the confidence score
     * @return Score from 0.0 to 1.0
     */
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    /**
     * Get confidence level as text
     * @return "Low", "Medium", or "High"
     */
    public String getConfidenceLevel() {
        if (confidenceScore >= 0.75) {
            return "High";
        } else if (confidenceScore >= 0.5) {
            return "Medium";
        } else {
            return "Low";
        }
    }
    
    // Utility Methods
    /**
     * Get a complete formatted cover letter
     * @return Formatted cover letter text
     */
    public String getFormattedLetter() {
        StringBuilder letter = new StringBuilder();
        
        // Opening
        letter.append(getOpening()).append("\n\n");
        
        // Body
        for (String paragraph : bodyParagraphs) {
            letter.append(paragraph).append("\n\n");
        }
        
        // Closing
        letter.append(getClosing()).append("\n\n");
        
        return letter.toString();
    }
    
    /**
     * Get a summary of the cover letter content
     * @return Summary text
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Cover Letter Summary:\n");
        summary.append("Tone: ").append(tone).append("\n");
        summary.append("Confidence: ").append(getConfidenceLevel()).append(" (");
        summary.append(String.format("%.0f%%", confidenceScore * 100)).append(")\n");
        summary.append("Body Paragraphs: ").append(bodyParagraphs.size()).append("\n");
        summary.append("Achievements: ").append(achievements.size()).append("\n");
        summary.append("Highlights: ").append(highlights.size()).append("\n");
        
        if (!customSections.isEmpty()) {
            summary.append("Custom Sections: ").append(customSections.keySet()).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if content is complete and ready to use
     * @return true if all required sections are present
     */
    public boolean isComplete() {
        return opening != null && !opening.isEmpty() &&
               !bodyParagraphs.isEmpty() &&
               closing != null && !closing.isEmpty();
    }
    
    /**
     * Get a checklist of what's included
     * @return List of included components
     */
    public List<String> getContentChecklist() {
        List<String> checklist = new ArrayList<>();
        
        if (opening != null && !opening.isEmpty()) {
            checklist.add("✓ Opening paragraph");
        }
        
        if (!bodyParagraphs.isEmpty()) {
            checklist.add("✓ Body paragraphs (" + bodyParagraphs.size() + ")");
        }
        
        if (closing != null && !closing.isEmpty()) {
            checklist.add("✓ Closing paragraph");
        }
        
        if (!achievements.isEmpty()) {
            checklist.add("✓ Key achievements");
        }
        
        if (!highlights.isEmpty()) {
            checklist.add("✓ Highlights");
        }
        
        return checklist;
    }
    
    @Override
    public String toString() {
        return "CoverLetterContent{" +
                "bodyParagraphs=" + bodyParagraphs.size() +
                ", achievements=" + achievements.size() +
                ", confidence=" + String.format("%.2f", confidenceScore) +
                ", tone='" + tone + '\'' +
                ", complete=" + isComplete() +
                '}';
    }
}