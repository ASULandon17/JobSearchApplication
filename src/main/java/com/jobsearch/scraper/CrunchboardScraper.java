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

public class CrunchboardScraper {
    private static final Logger logger = LoggerFactory.getLogger(CrunchboardScraper.class);
    private static final String BASE_URL = "https://www.crunchboard.com";
    
    public List<JobPosting> scrape(SearchFilters filters) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            String url = BASE_URL + "/jobs?query=" + 
                filters.getSearchTerms().replace(" ", "+");
            
            logger.info("Scraping Crunchboard: {}", url);
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .referrer("https://techcrunch.com")
                .followRedirects(true)
                .get();
            
            Elements jobListings = doc.select("div.job-listing, li.job, article");
            
            for (Element listing : jobListings) {
                try {
                    JobPosting job = parseCrunchboardJob(listing);
                    if (job != null) {
                        jobs.add(job);
                        logger.debug("Added Crunchboard job: {}", job.getTitle());
                    }
                    
                    if (jobs.size() >= 25) break;
                } catch (Exception e) {
                    logger.debug("Error parsing Crunchboard job: {}", e.getMessage());
                }
            }
            
            logger.info("Scraped {} jobs from Crunchboard", jobs.size());
            
        } catch (Exception e) {
            logger.error("Error scraping Crunchboard: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private JobPosting parseCrunchboardJob(Element element) {
        JobPosting job = new JobPosting();
        
        Element titleElement = element.selectFirst("h2, h3, a.job-title");
        if (titleElement == null) return null;
        job.setTitle(titleElement.text().trim());
        
        Element companyElement = element.selectFirst("span.company, div.company-name");
        if (companyElement != null) {
            job.setCompany(companyElement.text().trim());
        } else {
            job.setCompany("See posting");
        }
        
        Element locationElement = element.selectFirst("span.location");
        if (locationElement != null) {
            job.setLocation(locationElement.text().trim());
        } else {
            job.setLocation("See posting");
        }
        
        Element linkElement = element.selectFirst("a[href*='/jobs/'], a");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("/")) {
                job.setUrl(BASE_URL + href);
            } else if (href.startsWith("http")) {
                job.setUrl(href);
            } else {
                job.setUrl(BASE_URL + "/jobs");
            }
        }
        
        job.setSource("Crunchboard");
        job.setReputabilityScore(8);
        job.setPostedDate(LocalDate.now());
        
        return job;
    }
}