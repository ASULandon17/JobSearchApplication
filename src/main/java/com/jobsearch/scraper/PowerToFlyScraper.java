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

public class PowerToFlyScraper {
    private static final Logger logger = LoggerFactory.getLogger(PowerToFlyScraper.class);
    private static final String BASE_URL = "https://powertofly.com";
    
    public List<JobPosting> scrape(SearchFilters filters) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            String url = BASE_URL + "/jobs?keywords=" + 
                filters.getSearchTerms().replace(" ", "%20");
            
            // Add location filter if specified
            if (filters.hasLocationFilter()) {
                url += "&location=" + filters.getLocationString().replace(" ", "%20");
            }
            
            logger.info("Scraping PowerToFly: {}", url);
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .referrer("https://www.google.com")
                .followRedirects(true)
                .get();
            
            // PowerToFly uses various selectors depending on their current layout
            Elements jobCards = doc.select("div.job-card, div[class*='job'], article[class*='job']");
            
            if (jobCards.isEmpty()) {
                // Try alternative selectors
                jobCards = doc.select("a[href*='/jobs/detail/']").parents();
            }
            
            for (Element card : jobCards) {
                try {
                    JobPosting job = parsePowerToFlyJob(card);
                    if (job != null && matchesFilters(job, filters)) {
                        jobs.add(job);
                        logger.debug("Added PowerToFly job: {}", job.getTitle());
                    }
                    
                    if (jobs.size() >= 25) break;
                } catch (Exception e) {
                    logger.debug("Error parsing PowerToFly job: {}", e.getMessage());
                }
            }
            
            logger.info("Scraped {} jobs from PowerToFly", jobs.size());
            
        } catch (Exception e) {
            logger.error("Error scraping PowerToFly: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private JobPosting parsePowerToFlyJob(Element element) {
        JobPosting job = new JobPosting();
        
        // Try multiple selectors for title
        Element titleElement = element.selectFirst("h2, h3, a[href*='/jobs/detail/']");
        if (titleElement == null) return null;
        job.setTitle(titleElement.text().trim());
        
        // Company
        Element companyElement = element.selectFirst("span.company, div.company, p.company");
        if (companyElement != null) {
            job.setCompany(companyElement.text().trim());
        } else {
            job.setCompany("See posting");
        }
        
        // Location
        Element locationElement = element.selectFirst("span.location, div.location, p.location");
        if (locationElement != null) {
            job.setLocation(locationElement.text().trim());
        } else {
            job.setLocation("See posting");
        }
        
        // URL
        Element linkElement = element.selectFirst("a[href*='/jobs/detail/'], a[href*='/jobs/']");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("/")) {
                job.setUrl(BASE_URL + href);
            } else {
                job.setUrl(href);
            }
        } else {
            job.setUrl(BASE_URL + "/jobs");
        }
        
        job.setSource("PowerToFly");
        job.setReputabilityScore(8);
        job.setPostedDate(LocalDate.now());
        
        return job;
    }
    
    private boolean matchesFilters(JobPosting job, SearchFilters filters) {
        // Work model filter
        if (filters.getWorkModel() != SearchFilters.WorkModel.NO_PREFERENCE) {
            String location = job.getLocation().toLowerCase();
            
            switch (filters.getWorkModel()) {
                case REMOTE:
                    if (!location.contains("remote")) {
                        return false;
                    }
                    break;
                case HYBRID:
                    if (!location.contains("hybrid")) {
                        return false;
                    }
                    break;
                case IN_PERSON:
                    if (location.contains("remote") || location.contains("hybrid")) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
}