package com.jobsearch.scraper;

import com.jobsearch.model.JobPosting;
import com.jobsearch.model.SearchFilters;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            options.addArguments("--start-maximized");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "enable-logging"});
            options.setExperimentalOption("useAutomationExtension", false);
            
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            
            initialized = true;
            logger.info("Selenium WebDriver initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver: {}", e.getMessage());
            initialized = false;
        }
    }
    
    public List<JobPosting> scrapeLinkedInJobs(SearchFilters filters) {
        List<JobPosting> jobs = new ArrayList<>();
        
        if (!initialized) {
            logger.warn("Selenium not initialized, skipping LinkedIn");
            return jobs;
        }
        
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("https://www.linkedin.com/jobs/search/?keywords=");
            urlBuilder.append(filters.getSearchTerms().replace(" ", "%20"));
            
            if (filters.hasLocationFilter()) {
                urlBuilder.append("&location=").append(filters.getLocationString().replace(" ", "%20"));
                urlBuilder.append("&distance=50"); // 50 mile radius
            } else {
                urlBuilder.append("&location=");
            }
            
            // Add experience level filter
            if (filters.getExperienceLevel() != SearchFilters.ExperienceLevel.NO_PREFERENCE) {
                switch (filters.getExperienceLevel()) {
                    case JUNIOR:
                        urlBuilder.append("&f_E=1,2"); // Entry level, Associate
                        break;
                    case MID_LEVEL:
                        urlBuilder.append("&f_E=3"); // Mid-Senior level
                        break;
                    case SENIOR:
                        urlBuilder.append("&f_E=4,5,6"); // Senior, Director, Executive
                        break;
                }
            }
            
            // Add work model filter
            if (filters.getWorkModel() == SearchFilters.WorkModel.REMOTE) {
                urlBuilder.append("&f_WT=2"); // Remote filter
            } else if (filters.getWorkModel() == SearchFilters.WorkModel.HYBRID) {
                urlBuilder.append("&f_WT=3"); // Hybrid filter
            } else if (filters.getWorkModel() == SearchFilters.WorkModel.IN_PERSON) {
                urlBuilder.append("&f_WT=1"); // On-site filter
            }
            
            urlBuilder.append("&f_TPR=r86400"); // Past 24 hours
            
            String url = urlBuilder.toString();
            logger.info("Scraping LinkedIn Jobs: {}", url);
            driver.get(url);
            
            if (detectCaptcha()) {
                logger.warn("CAPTCHA detected on LinkedIn, skipping");
                return jobs;
            }
            
            Thread.sleep(5000); // Wait for page to load
            
            List<WebElement> jobCards = driver.findElements(By.cssSelector("div.base-card, div.job-search-card"));
            logger.info("Found {} job cards on LinkedIn", jobCards.size());
            
            for (int i = 0; i < Math.min(jobCards.size(), 25); i++) {
                try {
                    WebElement card = jobCards.get(i);
                    JobPosting job = parseLinkedInJob(card);
                    if (job != null && job.getTitle() != null) {
                        jobs.add(job);
                        logger.debug("Added LinkedIn job: {}", job.getTitle());
                    }
                } catch (Exception e) {
                    logger.debug("Error parsing LinkedIn job {}: {}", i, e.getMessage());
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
            WebElement titleElement = element.findElement(By.cssSelector("h3.base-search-card__title, span.sr-only"));
            job.setTitle(titleElement.getText().trim());
        } catch (Exception e) {
            logger.debug("Could not extract title from LinkedIn job");
            return null;
        }
        
        try {
            WebElement companyElement = element.findElement(By.cssSelector("h4.base-search-card__subtitle, a.hidden-nested-link"));
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
    
    public List<JobPosting> scrapeDice(SearchFilters filters) {
    List<JobPosting> jobs = new ArrayList<>();
    
    if (!initialized) {
        logger.warn("Selenium not initialized, skipping Dice");
        return jobs;
    }
    
    try {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://www.dice.com/jobs?q=");
        urlBuilder.append(filters.getSearchTerms().replace(" ", "%20"));
        
        if (filters.hasLocationFilter()) {
            urlBuilder.append("&location=").append(filters.getLocationString().replace(" ", "%20"));
            urlBuilder.append("&radius=50");
        }
        
        // Add work model filter
        if (filters.getWorkModel() == SearchFilters.WorkModel.REMOTE) {
            urlBuilder.append("&filters.workplaceTypes=Remote");
        }
        
        urlBuilder.append("&filters.postedDate=ONE");
        urlBuilder.append("&pageSize=25");
        
        String url = urlBuilder.toString();
        logger.info("Scraping Dice.com: {}", url);
        driver.get(url);
        
        if (detectCaptcha()) {
            logger.warn("CAPTCHA detected on Dice, skipping");
            return jobs;
        }
        
        // Wait longer for dynamic content
        Thread.sleep(8000);
        
        // Try to scroll to trigger lazy loading
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight/2);");
        Thread.sleep(2000);
        
        // Log page source for debugging
        logger.debug("Page title: {}", driver.getTitle());
        
        // Try multiple selectors
        List<WebElement> jobCards = driver.findElements(By.cssSelector("div[id^='card-']"));
        
        if (jobCards.isEmpty()) {
            jobCards = driver.findElements(By.cssSelector("div.card"));
        }
        
        if (jobCards.isEmpty()) {
            jobCards = driver.findElements(By.cssSelector("div[class*='job']"));
        }
        
        if (jobCards.isEmpty()) {
            // Try to find any links to job details
            jobCards = driver.findElements(By.cssSelector("a[href*='/job-detail/']")).stream()
                .map(link -> link.findElement(By.xpath("./..")))
                .collect(Collectors.toList());
        }
        
        logger.info("Found {} potential job elements on Dice", jobCards.size());
        
        for (int i = 0; i < Math.min(jobCards.size(), 25); i++) {
            try {
                WebElement card = jobCards.get(i);
                JobPosting job = parseDiceJob(card);
                if (job != null && job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                    logger.debug("Added Dice job: {}", job.getTitle());
                }
            } catch (Exception e) {
                logger.debug("Error parsing Dice job {}: {}", i, e.getMessage());
            }
        }
        
        if (jobs.isEmpty()) {
            // Log page source snippet for debugging
            String pageText = driver.findElement(By.tagName("body")).getText();
            logger.warn("Dice page content sample: {}", 
                pageText.substring(0, Math.min(500, pageText.length())));
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
        // Try multiple selectors for title and link
        WebElement titleLink = null;
        
        try {
            titleLink = element.findElement(By.cssSelector("a[id^='jobTitle']"));
        } catch (Exception e) {
            try {
                titleLink = element.findElement(By.cssSelector("a.card-title-link"));
            } catch (Exception ex) {
                try {
                    titleLink = element.findElement(By.cssSelector("a[href*='/job-detail/']"));
                } catch (Exception exc) {
                    Elements links = Jsoup.parse(element.getAttribute("outerHTML"))
                        .select("a[href*='/job-detail/']");
                    if (!links.isEmpty()) {
                        job.setTitle(links.first().text());
                        job.setUrl(links.first().attr("abs:href"));
                        titleLink = null; // Skip the WebElement part
                    }
                }
            }
        }
        
        if (titleLink != null) {
            job.setTitle(titleLink.getText().trim());
            job.setUrl(titleLink.getAttribute("href"));
        }
        
        if (job.getTitle() == null || job.getTitle().isEmpty()) {
            return null;
        }
        
    } catch (Exception e) {
        logger.debug("Could not extract title from Dice job: {}", e.getMessage());
        return null;
    }
    
    // Company
    try {
        WebElement companyElement = element.findElement(By.cssSelector("span.company, div.company, a.company"));
        job.setCompany(companyElement.getText().trim());
    } catch (Exception e) {
        job.setCompany("See posting");
    }
    
    // Location
    try {
        WebElement locationElement = element.findElement(By.cssSelector("span.location, div.location"));
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
                   pageSource.contains("access denied") ||
                   title.contains("captcha") ||
                   title.contains("security check") ||
                   title.contains("access denied") ||
                   title.contains("please verify");
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