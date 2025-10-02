package com.jobsearch.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jobsearch.model.JobPosting;
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
    // Sign up for free - 250 calls per month
    private static final String ADZUNA_APP_ID = "8a4ef2d1";
    private static final String ADZUNA_APP_KEY = "175b38794514f6c20f5d02751eb87f55";
    
    public JobBoardAPIClient() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
        logger.info("JobBoardAPIClient initialized");
    }
    
    public List<JobPosting> searchAdzuna(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        
        if (ADZUNA_APP_ID.equals("YOUR_APP_ID_HERE")) {
            logger.info("Adzuna API keys not configured. Get free keys at https://developer.adzuna.com/");
            logger.info("You get 250 free API calls per month!");
            return jobs;
        }
        
        try {
            String encodedTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
            String url = String.format(
                "https://api.adzuna.com/v1/api/jobs/us/search/1?app_id=%s&app_key=%s&results_per_page=25&what=%s&content-type=application/json",
                ADZUNA_APP_ID, ADZUNA_APP_KEY, encodedTerm
            );
            
            logger.info("Calling Adzuna API for: {}", searchTerm);
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Adzuna API error: HTTP {} - {}", response.code(), response.message());
                    if (response.code() == 401) {
                        logger.error("Authentication failed. Please check your Adzuna API credentials.");
                    }
                    return jobs;
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                if (!jsonResponse.has("results")) {
                    logger.warn("Adzuna API response missing 'results' field");
                    return jobs;
                }
                
                JsonArray results = jsonResponse.getAsJsonArray("results");
                
                for (JsonElement element : results) {
                    JsonObject jobJson = element.getAsJsonObject();
                    JobPosting job = parseAdzunaJob(jobJson);
                    if (job != null && job.getTitle() != null) {
                        jobs.add(job);
                    }
                }
                
                logger.info("Retrieved {} jobs from Adzuna API", jobs.size());
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
                // Salary parsing failed, skip it
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
    
    public List<JobPosting> searchRemotiveAPI(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        
        try {
            String encodedTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
            String url = "https://remotive.com/api/remote-jobs?search=" + encodedTerm + "&limit=50";
            
            logger.info("Calling Remotive API for: {}", searchTerm);
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", "application/json")
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Remotive API error: HTTP {}", response.code());
                    return jobs;
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                if (!jsonResponse.has("jobs")) {
                    logger.warn("Remotive API response missing 'jobs' field");
                    return jobs;
                }
                
                JsonArray jobsArray = jsonResponse.getAsJsonArray("jobs");
                
                for (JsonElement element : jobsArray) {
                    JsonObject jobJson = element.getAsJsonObject();
                    JobPosting job = parseRemotiveJob(jobJson);
                    if (job != null && job.getTitle() != null) {
                        jobs.add(job);
                    }
                }
                
                logger.info("Retrieved {} jobs from Remotive API", jobs.size());
            }
        } catch (Exception e) {
            logger.error("Error calling Remotive API: {}", e.getMessage());
        }
        
        return jobs;
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