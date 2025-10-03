package com.jobsearch.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jobsearch.model.JobPosting;
import com.jobsearch.model.SearchFilters;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JobBoardAPIClient {
    private static final Logger logger = LoggerFactory.getLogger(JobBoardAPIClient.class);
    private final OkHttpClient client;
    private final Gson gson;
    
    // Get free API keys from: https://developer.adzuna.com/
    private static final String ADZUNA_APP_ID = "8a4ef2d1";
    private static final String ADZUNA_APP_KEY = "175b38794514f6c20f5d02751eb87f55";
    
    public JobBoardAPIClient() {
    this.client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // Increased from 15
        .readTimeout(30, TimeUnit.SECONDS)      // Increased from 15
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build();
    this.gson = new Gson();
    logger.info("JobBoardAPIClient initialized");
}
    
    public List<JobPosting> searchAdzuna(SearchFilters filters) {
    List<JobPosting> jobs = new ArrayList<>();
    
    if (ADZUNA_APP_ID.equals("YOUR_APP_ID_HERE") || ADZUNA_APP_ID.isEmpty()) {
        logger.info("Adzuna API keys not configured");
        return jobs;
    }
    
    try {
        String encodedTerm = URLEncoder.encode(filters.getSearchTerms(), StandardCharsets.UTF_8.toString());
        
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://api.adzuna.com/v1/api/jobs/us/search/1?");
        urlBuilder.append("app_id=").append(ADZUNA_APP_ID);
        urlBuilder.append("&app_key=").append(ADZUNA_APP_KEY);
        urlBuilder.append("&results_per_page=50");
        urlBuilder.append("&what=").append(encodedTerm);
        
        // Only add location if specified
        if (filters.hasLocationFilter()) {
            String location = URLEncoder.encode(filters.getLocationString(), StandardCharsets.UTF_8.toString());
            urlBuilder.append("&where=").append(location);
            urlBuilder.append("&distance=50");
        }
        
        // DON'T add work model to the API query - filter after retrieval instead
        // The issue is that adding "remote" to the query over-restricts results
        
        String url = urlBuilder.toString();
        logger.info("Calling Adzuna API: {}", url);
        
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "JobSearchAssistant/1.0")
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Adzuna API error: HTTP {} - {}", response.code(), response.message());
                return jobs;
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            if (!jsonResponse.has("results")) {
                logger.warn("Adzuna API response missing 'results' field");
                return jobs;
            }
            
            JsonArray results = jsonResponse.getAsJsonArray("results");
            logger.info("Adzuna returned {} raw results", results.size());
            
            for (JsonElement element : results) {
                JsonObject jobJson = element.getAsJsonObject();
                JobPosting job = parseAdzunaJob(jobJson);
                if (job != null && matchesFilters(job, filters)) {
                    jobs.add(job);
                }
            }
            
            logger.info("Retrieved {} jobs from Adzuna API after filtering", jobs.size());
        }
    } catch (IOException e) {
        logger.error("Error calling Adzuna API: {}", e.getMessage());
    }
    
    return jobs;
}
    
    private JobPosting parseAdzunaJob(JsonObject jobJson) {
        JobPosting job = new JobPosting();
        
        job.setTitle(getJsonString(jobJson, "title"));
        
        JsonObject company = jobJson.getAsJsonObject("company");
        if (company != null) {
            job.setCompany(getJsonString(company, "display_name"));
        }
        
        JsonObject location = jobJson.getAsJsonObject("location");
        if (location != null) {
            List<String> locationParts = new ArrayList<>();
            String displayName = getJsonString(location, "display_name");
            if (displayName != null) {
                locationParts.add(displayName);
            }
            
            if (!locationParts.isEmpty()) {
                job.setLocation(String.join(", ", locationParts));
            }
        }
        
        if (jobJson.has("salary_min") && jobJson.has("salary_max")) {
            try {
                double min = jobJson.get("salary_min").getAsDouble();
                double max = jobJson.get("salary_max").getAsDouble();
                if (min > 0 && max > 0) {
                    job.setSalary(String.format("$%,.0f - $%,.0f", min, max));
                }
            } catch (Exception e) {
                // Salary parsing failed
            }
        }
        
        job.setUrl(getJsonString(jobJson, "redirect_url"));
        job.setDescription(getJsonString(jobJson, "description"));
        
        String dateStr = getJsonString(jobJson, "created");
        if (dateStr != null) {
            try {
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_DATE);
                job.setPostedDate(date);
            } catch (Exception e) {
                job.setPostedDate(LocalDate.now());
            }
        }
        
        job.setSource("Adzuna");
        job.setReputabilityScore(9);
        
        return job;
    }
    
    public List<JobPosting> searchRemotiveAPI(SearchFilters filters) {
    List<JobPosting> jobs = new ArrayList<>();
    
    try {
        String encodedTerm = URLEncoder.encode(filters.getSearchTerms(), StandardCharsets.UTF_8.toString());
        
        // Try the simpler API endpoint that's faster
        String url = "https://remotive.com/api/remote-jobs?category=software-dev&limit=50";
        
        logger.info("Calling Remotive API");
        
        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader("Referer", "https://remotive.com/")
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Remotive API error: HTTP {}", response.code());
                return jobs;
            }
            
            String responseBody = response.body().string();
            
            if (responseBody.trim().startsWith("<")) {
                logger.warn("Remotive returned HTML instead of JSON - possible blocking");
                return jobs;
            }
            
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            if (!jsonResponse.has("jobs")) {
                logger.warn("Remotive API response missing 'jobs' field");
                return jobs;
            }
            
            JsonArray jobsArray = jsonResponse.getAsJsonArray("jobs");
            logger.info("Remotive returned {} raw results", jobsArray.size());
            
            // Filter by search terms and other filters
            String searchLower = filters.getSearchTerms().toLowerCase();
            
            for (JsonElement element : jobsArray) {
                JsonObject jobJson = element.getAsJsonObject();
                
                // Check if job matches search terms
                String title = getJsonString(jobJson, "title");
                String description = getJsonString(jobJson, "description");
                
                if (title != null && description != null) {
                    String combined = (title + " " + description).toLowerCase();
                    if (combined.contains(searchLower) || 
                        containsAnyWord(combined, searchLower.split(" "))) {
                        
                        JobPosting job = parseRemotiveJob(jobJson);
                        if (job != null && matchesFilters(job, filters)) {
                            jobs.add(job);
                        }
                    }
                }
                
                if (jobs.size() >= 25) break;
            }
            
            logger.info("Retrieved {} jobs from Remotive API after filtering", jobs.size());
        }
    } catch (Exception e) {
        logger.error("Error calling Remotive API: {}", e.getMessage());
    }
    
    return jobs;
}

private boolean containsAnyWord(String text, String[] words) {
    for (String word : words) {
        if (word.length() > 2 && text.contains(word.toLowerCase())) {
            return true;
        }
    }
    return false;
}

    
    private JobPosting parseRemotiveJob(JsonObject jobJson) {
        JobPosting job = new JobPosting();
        
        job.setTitle(getJsonString(jobJson, "title"));
        job.setCompany(getJsonString(jobJson, "company_name"));
        job.setLocation("Remote");
        
        String salary = getJsonString(jobJson, "salary");
        if (salary != null && !salary.isEmpty()) {
            job.setSalary(salary);
        }
        
        job.setUrl(getJsonString(jobJson, "url"));
        job.setDescription(getJsonString(jobJson, "description"));
        
        String dateStr = getJsonString(jobJson, "publication_date");
        if (dateStr != null) {
            try {
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_DATE);
                job.setPostedDate(date);
            } catch (Exception e) {
                job.setPostedDate(LocalDate.now());
            }
        }
        
        job.setSource("Remotive");
        job.setReputabilityScore(8);
        
        return job;
    }
    
    private boolean matchesFilters(JobPosting job, SearchFilters filters) {
        // Experience level filtering
        if (filters.getExperienceLevel() != SearchFilters.ExperienceLevel.NO_PREFERENCE) {
            String title = job.getTitle().toLowerCase();
            String desc = job.getDescription() != null ? job.getDescription().toLowerCase() : "";
            String combined = title + " " + desc;
            
            switch (filters.getExperienceLevel()) {
                case JUNIOR:
                    if (!combined.contains("junior") && !combined.contains("entry") && 
                        !combined.contains("associate") && !combined.contains("jr")) {
                        return false;
                    }
                    break;
                case MID_LEVEL:
                    // Filter out junior and senior positions
                    if (combined.contains("senior") || combined.contains("lead") || 
                        combined.contains("principal") || combined.contains("junior") ||
                        combined.contains("entry")) {
                        return false;
                    }
                    break;
                case SENIOR:
                    if (!combined.contains("senior") && !combined.contains("lead") && 
                        !combined.contains("principal") && !combined.contains("staff") &&
                        !combined.contains("sr")) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
    
    private String getJsonString(JsonObject json, String key) {
        try {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        } catch (Exception e) {
            // Field might not be a string
        }
        return null;
    }
}