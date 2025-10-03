package com.jobsearch.analyzer;

import com.jobsearch.model.JobPosting;
import java.util.*;

public class JobAnalyzer {
    
    public void scoreJob(JobPosting job, String searchTerm) {
        int relevanceScore = calculateRelevanceScore(job, searchTerm);
        job.setRelevanceScore(relevanceScore);
    }
    
    private int calculateRelevanceScore(JobPosting job, String searchTerm) {
        String[] searchTerms = searchTerm.toLowerCase().split("\\s+");
        String jobTitle = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
        String jobDescription = job.getDescription() != null ? 
            job.getDescription().toLowerCase() : "";
        String combined = jobTitle + " " + jobDescription;
        
        int score = 0;
        int maxScore = 10;
        
        if (jobTitle.contains(searchTerm.toLowerCase())) {
            score = maxScore;
        } else {
            int matchCount = 0;
            for (String term : searchTerms) {
                if (combined.contains(term)) {
                    matchCount++;
                }
            }
            
            double matchPercentage = (double) matchCount / searchTerms.length;
            score = (int) (matchPercentage * maxScore);
            
            for (String term : searchTerms) {
                if (jobTitle.contains(term)) {
                    score = Math.min(score + 2, maxScore);
                }
            }
        }
        
        if (searchTerm.toLowerCase().contains("software") || 
            searchTerm.toLowerCase().contains("engineer")) {
            String[] relatedTerms = {"developer", "programmer", "coding", 
                                    "programming", "tech", "it"};
            for (String related : relatedTerms) {
                if (combined.contains(related)) {
                    score = Math.min(score + 1, maxScore);
                }
            }
        }
        
        return score;
    }
}