package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import com.jobsearch.model.SearchFilters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeWorkRemotelyScraper {
    private static final Logger logger = LoggerFactory.getLogger(WeWorkRemotelyScraper.class);
    private static final String BASE_URL = "https://weworkremotely.com";
    
    public List<JobPosting> scrape(SearchFilters filters) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            // Don't use search, scrape categories instead to avoid 403
            String url = BASE_URL + "/categories/remote-programming-jobs";
            
            logger.info("Scraping WeWorkRemotely: {}", url);
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Cache-Control", "max-age=0")
                .referrer("https://www.google.com/")
                .timeout(15000)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .get();
            
            // Check if we got blocked
            if (doc.title().toLowerCase().contains("access denied") || 
                doc.text().toLowerCase().contains("cloudflare")) {
                logger.warn("WeWorkRemotely blocked the request");
                return jobs;
            }
            
            // Look for job listings
            Elements jobListings = doc.select("li");
            String searchLower = filters.getSearchTerms().toLowerCase();
            
            for (Element listing : jobListings) {
                try {
                    // Check if this element contains a job link
                    Elements links = listing.select("a[href*='/remote-jobs/']");
                    if (links.isEmpty()) {
                        continue;
                    }
                    
                    String text = listing.text().toLowerCase();
                    
                    // Check if matches search terms
                    if (text.contains(searchLower) || 
                        containsAnyWord(text, searchLower.split(" "))) {
                        
                        JobPosting job = parseWeWorkJob(listing);
                        if (job != null && matchesFilters(job, filters)) {
                            jobs.add(job);
                            logger.debug("Added WeWorkRemotely job: {}", job.getTitle());
                        }
                    }
                    
                    if (jobs.size() >= 25) break;
                } catch (Exception e) {
                    logger.debug("Error parsing WeWorkRemotely job: {}", e.getMessage());
                }
            }
            
            logger.info("Scraped {} jobs from WeWorkRemotely", jobs.size());
            
        } catch (Exception e) {
            logger.error("Error scraping WeWorkRemotely: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private JobPosting parseWeWorkJob(Element element) {
        JobPosting job = new JobPosting();
        
        Element linkElement = element.selectFirst("a[href*='/remote-jobs/']");
        if (linkElement == null) return null;
        
        // Extract title from link text or look for specific elements
        String title = linkElement.text().trim();
        if (title.isEmpty()) {
            Element spanTitle = element.selectFirst("span.title");
            if (spanTitle != null) {
                title = spanTitle.text().trim();
            }
        }
        
        if (title.isEmpty()) return null;
        job.setTitle(title);
        
        // Company name
        Element companyElement = element.selectFirst("span.company");
        if (companyElement != null) {
            job.setCompany(companyElement.text().trim());
        } else {
            // Company might be in the link text before a pipe or dash
            if (title.contains("|")) {
                String[] parts = title.split("\\|");
                if (parts.length > 1) {
                    job.setCompany(parts[0].trim());
                    job.setTitle(parts[1].trim());
                }
            } else {
                job.setCompany("See posting");
            }
        }
        
        job.setLocation("Remote");
        
        String href = linkElement.attr("href");
        if (href.startsWith("/")) {
            job.setUrl(BASE_URL + href);
        } else {
            job.setUrl(href);
        }
        
        job.setSource("WeWorkRemotely");
        job.setReputabilityScore(9);
        job.setPostedDate(LocalDate.now());
        
        return job;
    }
    
    private boolean matchesFilters(JobPosting job, SearchFilters filters) {
        // Experience level check
        if (filters.getExperienceLevel() != SearchFilters.ExperienceLevel.NO_PREFERENCE) {
            String title = job.getTitle().toLowerCase();
            
            switch (filters.getExperienceLevel()) {
                case JUNIOR:
                    if (!title.contains("junior") && !title.contains("entry") && 
                        !title.contains("associate") && !title.contains("jr")) {
                        return false;
                    }
                    break;
                case MID_LEVEL:
                    if (title.contains("senior") || title.contains("lead") || 
                        title.contains("principal") || title.contains("junior") ||
                        title.contains("entry")) {
                        return false;
                    }
                    break;
                case SENIOR:
                    if (!title.contains("senior") && !title.contains("lead") && 
                        !title.contains("principal") && !title.contains("staff") &&
                        !title.contains("sr")) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
    
    private boolean containsAnyWord(String text, String[] words) {
        for (String word : words) {
            if (word.length() > 2 && text.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}