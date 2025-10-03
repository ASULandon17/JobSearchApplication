package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import com.jobsearch.analyzer.JobAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WebScraper {
    private static final int TIMEOUT = 10000;
    private final JobAnalyzer analyzer;
    
    private static final Map<String, Integer> SOURCES = new HashMap<String, Integer>() {{
        put("indeed.com", 9);
        put("linkedin.com/jobs", 10);
        put("glassdoor.com", 9);
        put("stackoverflow.com/jobs", 8);
        put("angel.co", 8);
        put("dice.com", 7);
        put("monster.com", 7);
        put("ziprecruiter.com", 7);
    }};
    
    public WebScraper() {
        this.analyzer = new JobAnalyzer();
    }
    
    public List<JobPosting> searchJobs(String searchTerm) {
        List<CompletableFuture<List<JobPosting>>> futures = new ArrayList<>();
        
        futures.add(CompletableFuture.supplyAsync(() -> 
            scrapeIndeed(searchTerm)));
        futures.add(CompletableFuture.supplyAsync(() -> 
            scrapeStackOverflow(searchTerm)));
        futures.add(CompletableFuture.supplyAsync(() -> 
            scrapeDemoJobs(searchTerm)));
        
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        
        try {
            allFutures.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        List<JobPosting> allJobs = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        allJobs.forEach(job -> analyzer.scoreJob(job, searchTerm));
        allJobs.sort((a, b) -> {
            int scoreA = a.getRelevanceScore() + a.getReputabilityScore();
            int scoreB = b.getRelevanceScore() + b.getReputabilityScore();
            return Integer.compare(scoreB, scoreA);
        });
        
        return allJobs;
    }
    
    private List<JobPosting> scrapeIndeed(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        String url = String.format("https://www.indeed.com/jobs?q=%s&l=", 
            searchTerm.replace(" ", "+"));
        
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(TIMEOUT)
                .get();
            
            Elements jobCards = doc.select("div.job_seen_beacon");
            
            for (Element card : jobCards) {
                JobPosting job = new JobPosting();
                
                Element titleElement = card.selectFirst("h2.jobTitle span[title]");
                if (titleElement != null) {
                    job.setTitle(titleElement.attr("title"));
                }
                
                Element companyElement = card.selectFirst("span.companyName");
                if (companyElement != null) {
                    job.setCompany(companyElement.text());
                }
                
                Element locationElement = card.selectFirst("div.companyLocation");
                if (locationElement != null) {
                    job.setLocation(locationElement.text());
                }
                
                Element salaryElement = card.selectFirst("div.salary-snippet");
                if (salaryElement != null) {
                    job.setSalary(salaryElement.text());
                }
                
                Element linkElement = card.selectFirst("a[href]");
                if (linkElement != null) {
                    String jobUrl = "https://www.indeed.com" + linkElement.attr("href");
                    job.setUrl(jobUrl);
                }
                
                job.setSource("Indeed");
                job.setReputabilityScore(SOURCES.get("indeed.com"));
                
                Element dateElement = card.selectFirst("span.date");
                if (dateElement != null) {
                    job.setPostedDate(parseRelativeDate(dateElement.text()));
                }
                
                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } catch (IOException e) {
            System.err.println("Error scraping Indeed: " + e.getMessage());
        }
        
        return jobs;
    }
    
    private List<JobPosting> scrapeStackOverflow(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        String url = String.format("https://stackoverflow.com/jobs?q=%s", 
            searchTerm.replace(" ", "+"));
        
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(TIMEOUT)
                .get();
            
            Elements jobListings = doc.select("div[data-jobid]");
            
            for (Element listing : jobListings) {
                JobPosting job = new JobPosting();
                
                Element titleElement = listing.selectFirst("h2 a");
                if (titleElement != null) {
                    job.setTitle(titleElement.text());
                    job.setUrl("https://stackoverflow.com" + titleElement.attr("href"));
                }
                
                Element companyElement = listing.selectFirst("span.fc-black-700");
                if (companyElement != null) {
                    job.setCompany(companyElement.text());
                }
                
                job.setSource("Stack Overflow");
                job.setReputabilityScore(SOURCES.get("stackoverflow.com/jobs"));
                
                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } catch (IOException e) {
            System.err.println("Error scraping Stack Overflow: " + e.getMessage());
        }
        
        return jobs;
    }
    
    private List<JobPosting> scrapeDemoJobs(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        
        // Create some demo jobs for testing purposes
        JobPosting job1 = new JobPosting();
        job1.setTitle("Junior Software Engineer");
        job1.setCompany("Tech Corp");
        job1.setLocation("Remote");
        job1.setSalary("$70,000 - $90,000");
        job1.setPostedDate(LocalDate.now().minusDays(2));
        job1.setUrl("https://example.com/job1");
        job1.setSource("Demo Board");
        job1.setReputabilityScore(8);
        jobs.add(job1);
        
        JobPosting job2 = new JobPosting();
        job2.setTitle("Software Developer");
        job2.setCompany("Innovation Labs");
        job2.setLocation("New York, NY");
        job2.setSalary("$80,000 - $100,000");
        job2.setPostedDate(LocalDate.now().minusDays(5));
        job2.setUrl("https://example.com/job2");
        job2.setSource("Demo Board");
        job2.setReputabilityScore(9);
        jobs.add(job2);
        
        JobPosting job3 = new JobPosting();
        job3.setTitle("Frontend Engineer");
        job3.setCompany("Web Solutions Inc");
        job3.setLocation("San Francisco, CA");
        job3.setSalary("$90,000 - $120,000");
        job3.setPostedDate(LocalDate.now().minusDays(1));
        job3.setUrl("https://example.com/job3");
        job3.setSource("Demo Board");
        job3.setReputabilityScore(7);
        jobs.add(job3);
        
        return jobs;
    }
    
    private LocalDate parseRelativeDate(String dateText) {
        LocalDate today = LocalDate.now();
        
        if (dateText.contains("today") || dateText.contains("just posted")) {
            return today;
        } else if (dateText.contains("yesterday")) {
            return today.minusDays(1);
        } else if (dateText.contains("day")) {
            try {
                int days = Integer.parseInt(dateText.replaceAll("[^0-9]", ""));
                return today.minusDays(days);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
}