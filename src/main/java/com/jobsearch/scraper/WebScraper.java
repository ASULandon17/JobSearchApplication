package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import com.jobsearch.analyzer.JobAnalyzer;
import com.jobsearch.api.JobBoardAPIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebScraper {
    private static final Logger logger = LoggerFactory.getLogger(WebScraper.class);
    private final JobAnalyzer analyzer;
    private final JobBoardAPIClient apiClient;
    private final HackerNewsScraper hnScraper;
    private SeleniumScraper seleniumScraper;
    
    public WebScraper() {
        this.analyzer = new JobAnalyzer();
        this.apiClient = new JobBoardAPIClient();
        this.hnScraper = new HackerNewsScraper();
        logger.info("WebScraper initialized - Production mode (no demo data)");
    }
    
    public List<JobPosting> searchJobs(String searchTerm) {
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║  STARTING JOB SEARCH: '{}'", searchTerm);
        logger.info("╚════════════════════════════════════════════════════════════╝");
        
        List<CompletableFuture<List<JobPosting>>> futures = new ArrayList<>();
        
        // API-based sources (most reliable, no bot detection)
        futures.add(CompletableFuture.supplyAsync(() -> 
            safeSearch("Adzuna API", () -> apiClient.searchAdzuna(searchTerm))));
        
        futures.add(CompletableFuture.supplyAsync(() -> 
            safeSearch("Remotive API", () -> apiClient.searchRemotiveAPI(searchTerm))));
        
        // Static HTML sources (works without JavaScript)
        futures.add(CompletableFuture.supplyAsync(() -> 
            safeSearch("HackerNews", () -> hnScraper.scrapeWhoIsHiring(searchTerm))));
        
        // Selenium-based sources (if available)
        futures.add(CompletableFuture.supplyAsync(() -> {
            List<JobPosting> seleniumJobs = new ArrayList<>();
            try {
                seleniumScraper = new SeleniumScraper();
                
                seleniumJobs.addAll(safeSearch("LinkedIn", () -> 
                    seleniumScraper.scrapeLinkedInJobs(searchTerm)));
                
                seleniumJobs.addAll(safeSearch("Dice", () -> 
                    seleniumScraper.scrapeDice(searchTerm)));
                
                return seleniumJobs;
            } catch (Exception e) {
                logger.warn("Selenium scraping encountered an error: {}", e.getMessage());
                return seleniumJobs;
            } finally {
                if (seleniumScraper != null) {
                    seleniumScraper.close();
                }
            }
        }));
        
        // Wait for all scrapers with timeout
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        
        try {
            allFutures.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Timeout or error waiting for scrapers: {}", e.getMessage());
        }
        
        // Collect all results
        List<JobPosting> allJobs = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .filter(job -> job.getTitle() != null && !job.getTitle().isEmpty())
            .collect(Collectors.toList());
        
        logger.info("────────────────────────────────────────────────────────────");
        logger.info("TOTAL JOBS COLLECTED: {}", allJobs.size());
        logger.info("────────────────────────────────────────────────────────────");
        
        // Log source breakdown
        Map<String, Long> sourceBreakdown = allJobs.stream()
            .collect(Collectors.groupingBy(JobPosting::getSource, Collectors.counting()));
        
        sourceBreakdown.forEach((source, count) -> 
            logger.info("  • {}: {} jobs", source, count));
        
        logger.info("────────────────────────────────────────────────────────────");
        
        // Score and sort
        allJobs.forEach(job -> analyzer.scoreJob(job, searchTerm));
        allJobs.sort((a, b) -> {
            int scoreA = a.getRelevanceScore() + a.getReputabilityScore();
            int scoreB = b.getRelevanceScore() + b.getReputabilityScore();
            return Integer.compare(scoreB, scoreA);
        });
        
        logger.info("✓ Jobs scored and sorted");
        logger.info("✓ Returning {} total jobs", allJobs.size());
        logger.info("════════════════════════════════════════════════════════════\n");
        
        return allJobs;
    }
    
    private List<JobPosting> safeSearch(String source, SearchFunction searchFunc) {
        try {
            logger.info("→ Starting search from: {}", source);
            long startTime = System.currentTimeMillis();
            
            List<JobPosting> results = searchFunc.search();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} returned {} jobs in {}ms", source, results.size(), duration);
            
            return results;
        } catch (Exception e) {
            logger.error("✗ Error searching {}: {}", source, e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @FunctionalInterface
    private interface SearchFunction {
        List<JobPosting> search() throws Exception;
    }
}