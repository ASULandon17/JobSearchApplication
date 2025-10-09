package com.jobsearch.nlp;

import com.jobsearch.coverletter.CompanyInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CompanyResearcher {
    private static final Logger logger = LoggerFactory.getLogger(CompanyResearcher.class);
    
    public CompanyInfo research(String companyName, String jobUrl) {
        CompanyInfo info = new CompanyInfo();
        info.setName(companyName);
        
        try {
            // Try to scrape company information from job posting
            Document doc = Jsoup.connect(jobUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();
            
            // Extract company description
            String description = extractDescription(doc);
            info.setDescription(description);
            
            // Extract industry
            String industry = extractIndustry(doc);
            info.setIndustry(industry);
            
            // Extract company size
            String size = extractCompanySize(doc);
            info.setSize(size);
            
            // Extract values/culture keywords
            List<String> values = extractValues(doc);
            info.setValues(values);
            
            logger.info("Researched company: {}", companyName);
            
        } catch (Exception e) {
            logger.warn("Could not research company {}: {}", companyName, e.getMessage());
            info.setDescription("A leading company in the industry");
        }
        
        return info;
    }
    
    private String extractDescription(Document doc) {
        // Try common selectors for company description
        String[] selectors = {
            "div.company-description",
            "div.about-company",
            "section.company-info",
            "div[class*='company'][class*='about']"
        };
        
        for (String selector : selectors) {
            if (doc.select(selector).first() != null) {
                String text = doc.select(selector).first().text();
                if (text.length() > 50) {
                    return text.substring(0, Math.min(500, text.length()));
                }
            }
        }
        
        return "An innovative company dedicated to excellence";
    }
    
    private String extractIndustry(Document doc) {
        String text = doc.text().toLowerCase();
        
        if (text.contains("software") || text.contains("technology")) {
            return "Technology";
        } else if (text.contains("finance") || text.contains("bank")) {
            return "Finance";
        } else if (text.contains("healthcare") || text.contains("medical")) {
            return "Healthcare";
        } else if (text.contains("retail") || text.contains("ecommerce")) {
            return "Retail";
        }
        
        return "Various Industries";
    }
    
    private String extractCompanySize(Document doc) {
        String text = doc.text();
        if (text.contains("Fortune 500") || text.contains("enterprise")) {
            return "Large (1000+ employees)";
        } else if (text.contains("startup")) {
            return "Startup (1-50 employees)";
        } else {
            return "Mid-size (50-1000 employees)";
        }
    }
    
    private List<String> extractValues(Document doc) {
        List<String> values = new ArrayList<>();
        String text = doc.text().toLowerCase();
        
        String[] possibleValues = {
            "innovation", "collaboration", "diversity", "integrity",
            "excellence", "customer-focused", "agile", "inclusive"
        };
        
        for (String value : possibleValues) {
            if (text.contains(value)) {
                values.add(value);
            }
        }
        
        return values;
    }
}