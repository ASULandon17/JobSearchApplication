package com.jobsearch.coverletter;

/**
 * Configuration settings for cover letter generation
 */
public class CoverLetterConfig {
    // Confidence thresholds
    public static final double MIN_CONFIDENCE_TO_GENERATE = 0.3;
    public static final double HIGH_CONFIDENCE_THRESHOLD = 0.8;
    
    // Match score thresholds
    public static final double SKILL_MATCH_THRESHOLD = 0.5;
    public static final double EXPERIENCE_MATCH_THRESHOLD = 0.7;
    
    // Content generation
    public static final int MIN_BODY_PARAGRAPHS = 2;
    public static final int MAX_BODY_PARAGRAPHS = 4;
    public static final int MIN_ACHIEVEMENTS_TO_HIGHLIGHT = 2;
    public static final int MAX_ACHIEVEMENTS_TO_HIGHLIGHT = 5;
    
    // Model settings
    public static final boolean USE_ML_MODEL = true;
    public static final boolean USE_COMPANY_RESEARCH = true;
    public static final boolean USE_FALLBACK_TEMPLATES = true;
    
    // Output settings
    public static final String DEFAULT_TONE = "Professional";
    public static final int OPTIMAL_WORD_COUNT = 250;
    public static final int MIN_WORD_COUNT = 150;
    public static final int MAX_WORD_COUNT = 400;
}