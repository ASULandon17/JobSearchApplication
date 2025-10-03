package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import com.jobsearch.model.SearchFilters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HackerNewsScraper {
    private static final Logger logger = LoggerFactory.getLogger(HackerNewsScraper.class);
    
    public List<JobPosting> scrapeWhoIsHiring(SearchFilters filters) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            String url = "https://news.ycombinator.com/submitted?id=whoishiring";
            logger.info("Scraping HackerNews Who is Hiring");
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
            
            Elements submissions = doc.select("tr.athing");
            String latestJobThread = null;
            
            for (Element submission : submissions) {
                Element titleLink = submission.selectFirst("span.titleline a");
                if (titleLink != null && titleLink.text().contains("Who is hiring?")) {
                    latestJobThread = "https://news.ycombinator.com/item?id=" + 
                        submission.attr("id");
                    break;
                }
            }
            
            if (latestJobThread == null) {
                logger.warn("Could not find recent 'Who is hiring?' thread");
                return jobs;
            }
            
            logger.info("Found latest hiring thread: {}", latestJobThread);
            
            // Add delay before fetching thread
            Thread.sleep(1000);
            
            Document jobsDoc = Jsoup.connect(latestJobThread)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(20000)
                .get();
            
            Elements comments = jobsDoc.select("tr.comtr");
            String searchLower = filters.getSearchTerms().toLowerCase();
            
            logger.info("Processing {} comments from HackerNews", comments.size());
            
            for (Element comment : comments) {
                try {
                    String text = comment.text().toLowerCase();
                    
                    if (text.contains(searchLower) || containsAnyWord(text, searchLower.split(" "))) {
                        JobPosting job = parseHNComment(comment, latestJobThread);
                        if (job != null && matchesFilters(job, filters)) {
                            jobs.add(job);
                            logger.debug("Added HN job: {}", job.getTitle());
                        }
                    }
                    
                    if (jobs.size() >= 40) break;
                } catch (Exception e) {
                    logger.debug("Error parsing HN comment: {}", e.getMessage());
                }
            }
            
            logger.info("Scraped {} relevant jobs from HackerNews", jobs.size());
            
        } catch (IOException e) {
            logger.error("Error scraping HackerNews: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return jobs;
    }
    
    private JobPosting parseHNComment(Element comment, String threadUrl) {
        try {
            JobPosting job = new JobPosting();
            
            Element commentText = comment.selectFirst("div.comment");
            if (commentText == null) return null;
            
            String text = commentText.text();
            if (text.isEmpty()) return null;
            
            String[] parts = text.split("\\|");
            if (parts.length > 0 && parts[0].length() > 0) {
                String companyPart = parts[0].trim();
                job.setCompany(companyPart.substring(0, Math.min(50, companyPart.length())));
            } else {
                job.setCompany("See posting");
            }
            
            String[] lines = text.split("\\n");
            if (lines.length > 0 && lines[0].length() > 0) {
                String title = lines[0].trim();
                job.setTitle(title.substring(0, Math.min(100, title.length())));
            } else {
                job.setTitle("Software Position at " + job.getCompany());
            }
            
            String textLower = text.toLowerCase();
            if (textLower.contains("remote")) {
                job.setLocation("Remote");
            } else if (textLower.contains("on-site") || textLower.contains("onsite")) {
                job.setLocation("On-site");
            } else if (textLower.contains("hybrid")) {
                job.setLocation("Hybrid");
            } else {
                job.setLocation("See posting");
            }
            
            Element ageElement = comment.selectFirst("span.age a");
            if (ageElement != null) {
                job.setUrl("https://news.ycombinator.com/" + ageElement.attr("href"));
            } else {
                job.setUrl(threadUrl);
            }
            
            if (text.length() > 0) {
                job.setDescription(text.substring(0, Math.min(500, text.length())));
            }
            
            job.setSource("HackerNews");
            job.setReputabilityScore(8);
            job.setPostedDate(LocalDate.now());
            
            return job;
            
        } catch (Exception e) {
            logger.debug("Error parsing HN comment: {}", e.getMessage());
            return null;
        }
    }
    
    private boolean matchesFilters(JobPosting job, SearchFilters filters) {
        // Work model filter
        if (filters.getWorkModel() != SearchFilters.WorkModel.NO_PREFERENCE) {
            String location = job.getLocation().toLowerCase();
            String desc = job.getDescription() != null ? job.getDescription().toLowerCase() : "";
            String combined = location + " " + desc;
            
            switch (filters.getWorkModel()) {
                case REMOTE:
                    if (!combined.contains("remote")) {
                        return false;
                    }
                    break;
                case HYBRID:
                    if (!combined.contains("hybrid")) {
                        return false;
                    }
                    break;
                case IN_PERSON:
                    if (combined.contains("remote") || combined.contains("hybrid")) {
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