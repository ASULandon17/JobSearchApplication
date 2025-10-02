package com.jobsearch.analyzer;

import com.jobsearch.model.JobPosting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class JobAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(JobAnalyzer.class);
    
    public void scoreJob(JobPosting job, String searchTerm) {
        logger.debug("Scoring job: '{}' for search term: '{}'", 
            job.getTitle(), searchTerm);
        
        int relevanceScore = calculateRelevanceScore(job, searchTerm);
        job.setRelevanceScore(relevanceScore);
        
        logger.debug("Job '{}' scored: Relevance={}, Reputation={}", 
            job.getTitle(), relevanceScore, job.getReputabilityScore());
    }
    
    private int calculateRelevanceScore(JobPosting job, String searchTerm) {
        String[] searchTerms = searchTerm.toLowerCase().split("\\s+");
        String jobTitle = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
        String jobDescription = job.getDescription() != null ? 
            job.getDescription().toLowerCase() : "";
        String combined = jobTitle + " " + jobDescription;
        
        int score = 0;
        int maxScore = 10;
        
        // Exact match in title
        if (jobTitle.contains(searchTerm.toLowerCase())) {
            score = maxScore;
            logger.debug("Exact match found in title for '{}'", job.getTitle());
        } else {
            // Count matching terms
            int matchCount = 0;
            for (String term : searchTerms) {
                if (combined.contains(term)) {
                    matchCount++;
                }
            }
            
            double matchPercentage = (double) matchCount / searchTerms.length;
            score = (int) (matchPercentage * maxScore);
            
            // Bonus for title matches
            for (String term : searchTerms) {
                if (jobTitle.contains(term)) {
                    score = Math.min(score + 2, maxScore);
                }
            }
            
            logger.debug("Partial match: {} of {} terms matched", 
                matchCount, searchTerms.length);
        }
        
        // Check for related terms
        if (searchTerm.toLowerCase().contains("software") || 
            searchTerm.toLowerCase().contains("engineer")) {
            String[] relatedTerms = {"developer", "programmer", "coding", 
                                    "programming", "tech", "it", "dev"};
            for (String related : relatedTerms) {
                if (combined.contains(related)) {
                    score = Math.min(score + 1, maxScore);
                }
            }
        }
        
        return score;
    }
}