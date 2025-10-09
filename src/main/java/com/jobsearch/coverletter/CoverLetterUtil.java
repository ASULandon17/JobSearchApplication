package com.jobsearch.coverletter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods for cover letter generation
 */
public class CoverLetterUtil {
    
    /**
     * Count words in a string
     * @param text The text to count
     * @return Word count
     */
    public static int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
    
    /**
     * Calculate Levenshtein distance between two strings (for skill matching)
     * @param s1 First string
     * @param s2 Second string
     * @return Similarity score (0.0 to 1.0)
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        String str1 = s1.toLowerCase();
        String str2 = s2.toLowerCase();
        
        int longer = Math.max(str1.length(), str2.length());
        if (longer == 0) {
            return 1.0;
        }
        
        return (longer - levenshteinDistance(str1, str2)) / (double) longer;
    }
    
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dist = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dist[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dist[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dist[i][j] = Math.min(Math.min(
                    dist[i - 1][j] + 1,     // deletion
                    dist[i][j - 1] + 1),   // insertion
                    dist[i - 1][j - 1] + cost); // substitution
            }
        }
        
        return dist[s1.length()][s2.length()];
    }
    
    /**
     * Find best matching skills between two lists
     * @param required Required skills
     * @param available Available skills
     * @return List of matched skills
     */
    public static List<String> matchSkills(List<String> required, List<String> available) {
        List<String> matched = new ArrayList<>();
        
        for (String req : required) {
            for (String avail : available) {
                if (calculateSimilarity(req, avail) > 0.85) {
                    matched.add(avail);
                    break;
                }
            }
        }
        
        return matched;
    }
    
    /**
     * Extract keywords from text
     * @param text The text to extract from
     * @param minLength Minimum keyword length
     * @return List of keywords
     */
    public static List<String> extractKeywords(String text, int minLength) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(word -> word.length() >= minLength)
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate overall match score
     * @param result The skill match result
     * @return Score from 0.0 to 1.0
     */
    public static double calculateOverallScore(SkillMatchResult result) {
        double technicalScore = (double) result.getMatchedSkillsCount() /
            Math.max(1, result.getMatchedSkillsCount() + result.getUnmatchedSkillsCount());
        
        double softSkillsScore = result.getMatchedSoftSkillsCount() > 0 ? 1.0 : 0.5;
        
        double experienceScore = result.isExperienceMatch() ? 1.0 : 0.5;
        
        return (technicalScore * 0.5) + (softSkillsScore * 0.2) + (experienceScore * 0.3);
    }
    
    /**
     * Sanitize text for inclusion in cover letter
     * @param text The text to sanitize
     * @return Cleaned text
     */
    public static String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        
        return text.trim()
            .replaceAll("\\s+", " ")
            .replaceAll("[^\\w\\s.,!?'-]", "");
    }
    
    /**
     * Capitalize first letter of string
     * @param str The string
     * @return Capitalized string
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}