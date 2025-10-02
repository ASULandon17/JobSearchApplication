package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
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
    
    public List<JobPosting> scrapeWhoIsHiring(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            String url = "https://news.ycombinator.com/submitted?id=whoishiring";
            logger.info("Scraping HackerNews Who is Hiring");
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
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
            
            Document jobsDoc = Jsoup.connect(latestJobThread)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
            
            Elements comments = jobsDoc.select("tr.comtr");
            String searchLower = searchTerm.toLowerCase();
            
            logger.info("Processing {} comments from HackerNews", comments.size());
            
            for (Element comment : comments) {
                try {
                    String text = comment.text().toLowerCase();
                    
                    if (text.contains(searchLower) || containsAnyWord(text, searchLower.split(" "))) {
                        JobPosting job = parseHNComment(comment, latestJobThread);
                        if (job != null && job.getTitle() != null) {
                            jobs.add(job);
                            logger.debug("Added HN job: {}", job.getTitle());
                        }
                    }
                    
                    if (jobs.size() >= 30) break;
                } catch (Exception e) {
                    logger.debug("Error parsing HN comment: {}", e.getMessage());
                }
            }
            
            logger.info("Scraped {} relevant jobs from HackerNews", jobs.size());
            
        } catch (IOException e) {
            logger.error("Error scraping HackerNews: {}", e.getMessage());
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
            
            // Extract company name - usually the first part before | or -
            String[] parts = text.split("\\|");
            if (parts.length > 0 && parts[0].length() > 0) {
                String companyPart = parts[0].trim();
                job.setCompany(companyPart.substring(0, Math.min(50, companyPart.length())));
            } else {
                job.setCompany("See posting");
            }
            
            // Create title from first line
            String[] lines = text.split("\\n");
            if (lines.length > 0 && lines[0].length() > 0) {
                String title = lines[0].trim();
                job.setTitle(title.substring(0, Math.min(100, title.length())));
            } else {
                job.setTitle("Software Position at " + job.getCompany());
            }
            
            // Check for remote
            if (text.toLowerCase().contains("remote")) {
                job.setLocation("Remote");
            } else if (text.toLowerCase().contains("on-site") || text.toLowerCase().contains("onsite")) {
                job.setLocation("On-site");
            } else {
                job.setLocation("See posting");
            }
            
            // Get comment URL
            Element ageElement = comment.selectFirst("span.age a");
            if (ageElement != null) {
                job.setUrl("https://news.ycombinator.com/" + ageElement.attr("href"));
            } else {
                job.setUrl(threadUrl);
            }
            
            // Set description
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
    
    private boolean containsAnyWord(String text, String[] words) {
        for (String word : words) {
            if (word.length() > 2 && text.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}