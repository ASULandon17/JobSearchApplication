package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import com.jobsearch.model.SearchFilters;
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
    private final WeWorkRemotelyScraper weWorkScraper;
    private final PowerToFlyScraper powerToFlyScraper;
    private final CrunchboardScraper crunchboardScraper;
    private SeleniumScraper seleniumScraper;
    private final IndeedScraper indeedScraper;

public WebScraper() {
    this.analyzer = new JobAnalyzer();
    this.apiClient = new JobBoardAPIClient();
    this.hnScraper = new HackerNewsScraper();
    this.weWorkScraper = new WeWorkRemotelyScraper();
    this.powerToFlyScraper = new PowerToFlyScraper();
    this.crunchboardScraper = new CrunchboardScraper();
    this.indeedScraper = new IndeedScraper(); // Add this
    logger.info("WebScraper initialized with all job boards");
}
    
    public List<JobPosting> searchJobs(SearchFilters filters) {
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║  STARTING JOB SEARCH");
        logger.info("║  Search Terms: '{}'", filters.getSearchTerms());
        logger.info("║  Work Model: {}", filters.getWorkModel());
        logger.info("║  Location: {}", filters.hasLocationFilter() ? filters.getLocationString() : "Any");
        logger.info("║  Experience: {}", filters.getExperienceLevel());
        logger.info("╚════════════════════════════════════════════════════════════╝");
        
        List<CompletableFuture<List<JobPosting>>> futures = new ArrayList<>();
        
        // API-based sources (most reliable)
        futures.add(CompletableFuture.supplyAsync(() -> 
            safeSearch("Adzuna API", () -> apiClient.searchAdzuna(filters))));
        
        futures.add(CompletableFuture.supplyAsync(() -> {
            delay(1000); // Respectful delay
            return safeSearch("Remotive API", () -> apiClient.searchRemotiveAPI(filters));
        }));
        
        // Static HTML sources
        futures.add(CompletableFuture.supplyAsync(() -> {
            delay(1500);
            return safeSearch("HackerNews", () -> hnScraper.scrapeWhoIsHiring(filters));
        }));
        
        futures.add(CompletableFuture.supplyAsync(() -> {
            delay(2000);
            return safeSearch("WeWorkRemotely", () -> weWorkScraper.scrape(filters));
        }));
        
        futures.add(CompletableFuture.supplyAsync(() -> {
            delay(2500);
            return safeSearch("PowerToFly", () -> powerToFlyScraper.scrape(filters));
        }));
        
        futures.add(CompletableFuture.supplyAsync(() -> {
            delay(3000);
            return safeSearch("Crunchboard", () -> crunchboardScraper.scrape(filters));
        }));

        futures.add(CompletableFuture.supplyAsync(() -> {
    delay(3500);
    return safeSearch("Indeed", () -> indeedScraper.scrape(filters));
}));
        
        // Selenium-based sources (if available)
        futures.add(CompletableFuture.supplyAsync(() -> {
            List<JobPosting> seleniumJobs = new ArrayList<>();
            try {
                seleniumScraper = new SeleniumScraper();
                
                seleniumJobs.addAll(safeSearch("LinkedIn", () -> 
                    seleniumScraper.scrapeLinkedInJobs(filters)));
                
                delay(3000); // Delay between Selenium scrapes
                
                seleniumJobs.addAll(safeSearch("Dice", () -> 
                    seleniumScraper.scrapeDice(filters)));
                
                return seleniumJobs;
            } catch (Exception e) {
                logger.warn("Selenium scraping error: {}", e.getMessage());
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
            allFutures.get(90, TimeUnit.SECONDS);
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
        allJobs.forEach(job -> analyzer.scoreJob(job, filters.getSearchTerms()));
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
            return new ArrayList<>();
        }
    }
    
    private void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @FunctionalInterface
    private interface SearchFunction {
        List<JobPosting> search() throws Exception;
    }
}