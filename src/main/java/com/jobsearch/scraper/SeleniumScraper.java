package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeleniumScraper {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumScraper.class);
    private WebDriver driver;
    private boolean initialized = false;
    
    public SeleniumScraper() {
        try {
            initializeDriver();
        } catch (Exception e) {
            logger.error("Failed to initialize Selenium: {}", e.getMessage());
            logger.info("Selenium scraping will be disabled");
        }
    }
    
    private void initializeDriver() {
        try {
            logger.info("Initializing Chrome WebDriver...");
            WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);
            
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            
            initialized = true;
            logger.info("Selenium WebDriver initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver: {}", e.getMessage());
            initialized = false;
        }
    }
    
    public List<JobPosting> scrapeLinkedInJobs(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        
        if (!initialized) {
            logger.warn("Selenium not initialized, skipping LinkedIn");
            return jobs;
        }
        
        try {
            String url = "https://www.linkedin.com/jobs/search/?keywords=" + 
                searchTerm.replace(" ", "%20") + "&location=&f_TPR=r86400";
            
            logger.info("Scraping LinkedIn Jobs: {}", url);
            driver.get(url);
            
            if (detectCaptcha()) {
                logger.warn("CAPTCHA detected on LinkedIn, skipping");
                return jobs;
            }
            
            Thread.sleep(3000);
            
            List<WebElement> jobCards = driver.findElements(By.cssSelector("div.base-card"));
            logger.info("Found {} job cards on LinkedIn", jobCards.size());
            
            for (int i = 0; i < Math.min(jobCards.size(), 20); i++) {
                try {
                    WebElement card = jobCards.get(i);
                    JobPosting job = parseLinkedInJob(card);
                    if (job != null && job.getTitle() != null) {
                        jobs.add(job);
                        logger.debug("Added LinkedIn job: {}", job.getTitle());
                    }
                } catch (Exception e) {
                    logger.debug("Error parsing LinkedIn job: {}", e.getMessage());
                }
            }
            
            logger.info("Successfully scraped {} jobs from LinkedIn", jobs.size());
        } catch (Exception e) {
            logger.error("Error scraping LinkedIn: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private JobPosting parseLinkedInJob(WebElement element) {
        JobPosting job = new JobPosting();
        
        try {
            WebElement titleElement = element.findElement(By.cssSelector("h3.base-search-card__title"));
            job.setTitle(titleElement.getText().trim());
        } catch (Exception e) {
            logger.debug("Could not extract title from LinkedIn job");
            return null;
        }
        
        try {
            WebElement companyElement = element.findElement(By.cssSelector("h4.base-search-card__subtitle"));
            job.setCompany(companyElement.getText().trim());
        } catch (Exception e) {
            job.setCompany("See posting");
        }
        
        try {
            WebElement locationElement = element.findElement(By.cssSelector("span.job-search-card__location"));
            job.setLocation(locationElement.getText().trim());
        } catch (Exception e) {
            job.setLocation("Not specified");
        }
        
        try {
            WebElement linkElement = element.findElement(By.cssSelector("a.base-card__full-link"));
            job.setUrl(linkElement.getAttribute("href"));
        } catch (Exception e) {
            job.setUrl("https://www.linkedin.com/jobs/");
        }
        
        job.setSource("LinkedIn");
        job.setReputabilityScore(10);
        job.setPostedDate(LocalDate.now());
        
        return job;
    }
    
    public List<JobPosting> scrapeDice(String searchTerm) {
        List<JobPosting> jobs = new ArrayList<>();
        
        if (!initialized) {
            logger.warn("Selenium not initialized, skipping Dice");
            return jobs;
        }
        
        try {
            String url = "https://www.dice.com/jobs?q=" + searchTerm.replace(" ", "%20") + "&location=&latitude=&longitude=&countryCode=US&locationPrecision=Country&radius=30&radiusUnit=mi&page=1&pageSize=20&filters.postedDate=ONE&language=en";
            
            logger.info("Scraping Dice.com: {}", url);
            driver.get(url);
            
            if (detectCaptcha()) {
                logger.warn("CAPTCHA detected on Dice, skipping");
                return jobs;
            }
            
            Thread.sleep(3000);
            
            List<WebElement> jobCards = driver.findElements(By.cssSelector("dhi-search-card"));
            
            if (jobCards.isEmpty()) {
                jobCards = driver.findElements(By.cssSelector("div[data-cy='search-card']"));
            }
            
            logger.info("Found {} job cards on Dice", jobCards.size());
            
            for (int i = 0; i < Math.min(jobCards.size(), 20); i++) {
                try {
                    WebElement card = jobCards.get(i);
                    JobPosting job = parseDiceJob(card);
                    if (job != null && job.getTitle() != null) {
                        jobs.add(job);
                        logger.debug("Added Dice job: {}", job.getTitle());
                    }
                } catch (Exception e) {
                    logger.debug("Error parsing Dice job: {}", e.getMessage());
                }
            }
            
            logger.info("Successfully scraped {} jobs from Dice", jobs.size());
        } catch (Exception e) {
            logger.error("Error scraping Dice: {}", e.getMessage());
        }
        
        return jobs;
    }
    
    private JobPosting parseDiceJob(WebElement element) {
        JobPosting job = new JobPosting();
        
        try {
            WebElement titleLink = element.findElement(By.cssSelector("a[data-cy='card-title-link']"));
            job.setTitle(titleLink.getText().trim());
            job.setUrl(titleLink.getAttribute("href"));
        } catch (Exception e) {
            try {
                WebElement titleAlt = element.findElement(By.cssSelector("h5 a"));
                job.setTitle(titleAlt.getText().trim());
                job.setUrl(titleAlt.getAttribute("href"));
            } catch (Exception ex) {
                logger.debug("Could not extract title from Dice job");
                return null;
            }
        }
        
        try {
            WebElement companyElement = element.findElement(By.cssSelector("span[data-cy='search-result-company-name']"));
            job.setCompany(companyElement.getText().trim());
        } catch (Exception e) {
            job.setCompany("See posting");
        }
        
        try {
            WebElement locationElement = element.findElement(By.cssSelector("span[data-cy='search-result-location']"));
            job.setLocation(locationElement.getText().trim());
        } catch (Exception e) {
            job.setLocation("Not specified");
        }
        
        job.setSource("Dice");
        job.setReputabilityScore(8);
        job.setPostedDate(LocalDate.now());
        
        return job;
    }
    
    private boolean detectCaptcha() {
        try {
            String pageSource = driver.getPageSource().toLowerCase();
            String title = driver.getTitle().toLowerCase();
            
            return pageSource.contains("captcha") || 
                   pageSource.contains("recaptcha") ||
                   pageSource.contains("hcaptcha") ||
                   pageSource.contains("cloudflare") ||
                   title.contains("captcha") ||
                   title.contains("security check") ||
                   title.contains("access denied");
        } catch (Exception e) {
            return false;
        }
    }
    
    public void close() {
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver closed successfully");
            } catch (Exception e) {
                logger.error("Error closing WebDriver: {}", e.getMessage());
            }
        }
    }
}