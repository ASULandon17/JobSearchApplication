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

public class IndeedScraper {
    private static final Logger logger = LoggerFactory.getLogger(IndeedScraper.class);
    
    public List<JobPosting> scrape(SearchFilters filters) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("https://www.indeed.com/jobs?q=");
            urlBuilder.append(filters.getSearchTerms().replace(" ", "+"));
            
            if (filters.hasLocationFilter()) {
                urlBuilder.append("&l=").append(filters.getLocationString().replace(" ", "+"));
            }
            
            if (filters.getWorkModel() == SearchFilters.WorkModel.REMOTE) {
                urlBuilder.append("&remotejob=1");
            }
            
            urlBuilder.append("&fromage=1"); // Last 24 hours
            
            String url = urlBuilder.toString();
            logger.info("Scraping Indeed: {}", url);
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .referrer("https://www.google.com/")
                .timeout(15000)
                .get();
            
            Elements jobCards = doc.select("div.job_seen_beacon");
            
            if (jobCards.isEmpty()) {
                jobCards = doc.select("div.jobsearch-SerpJobCard");
            }
            
            for (Element card : jobCards) {
                try {
                    JobPosting job = parseIndeedJob(card);
                    if (job != null && matchesFilters(job, filters)) {
                        jobs.add(job);
                        logger.debug("Added Indeed job: {}", job.getTitle());
                    }
                    
                    if (jobs.size() >= 20) break;
                } catch (Exception e) {
                    logger.debug("Error parsing Indeed job: {}", e.getMessage());
                }
            }
            
            logger.info("Scraped {} jobs from Indeed", jobs.size());
            
        } catch (Exception e) {
            logger.error("Error scraping Indeed: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private JobPosting parseIndeedJob(Element element) {
        JobPosting job = new JobPosting();
        
        Element titleElement = element.selectFirst("h2.jobTitle a, h2.jobTitle span");
        if (titleElement == null) return null;
        job.setTitle(titleElement.text().trim());
        
        Element companyElement = element.selectFirst("span.companyName");
        if (companyElement != null) {
            job.setCompany(companyElement.text().trim());
        } else {
            job.setCompany("See posting");
        }
        
        Element locationElement = element.selectFirst("div.companyLocation");
        if (locationElement != null) {
            job.setLocation(locationElement.text().trim());
        } else {
            job.setLocation("Not specified");
        }
        
        Element linkElement = element.selectFirst("a");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("/")) {
                job.setUrl("https://www.indeed.com" + href);
            } else {
                job.setUrl(href);
            }
        }
        
        Element salaryElement = element.selectFirst("div.salary-snippet");
        if (salaryElement != null) {
            job.setSalary(salaryElement.text().trim());
        }
        
        job.setSource("Indeed");
        job.setReputabilityScore(9);
        job.setPostedDate(LocalDate.now());
        
        return job;
    }
    
    private boolean matchesFilters(JobPosting job, SearchFilters filters) {
        if (filters.getExperienceLevel() != SearchFilters.ExperienceLevel.NO_PREFERENCE) {
            String title = job.getTitle().toLowerCase();
            
            switch (filters.getExperienceLevel()) {
                case JUNIOR:
                    if (!title.contains("junior") && !title.contains("entry") && 
                        !title.contains("associate")) {
                        return false;
                    }
                    break;
                case SENIOR:
                    if (!title.contains("senior") && !title.contains("lead") && 
                        !title.contains("principal")) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
}