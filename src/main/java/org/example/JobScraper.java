package org.example;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.*;

public class JobScraper {

    // Companies mapped with urls
    private static final Map<String, String> COMPANY_SITES = new HashMap<>();

    static {
        COMPANY_SITES.put("Northern Trust", "https://ntrs.wd1.myworkdayjobs.com/northerntrust?q=java&locationCountry=c4f78be1a8f14da0ab49ce1162348a5e&locationRegionStateProvince=a3c37012f51642f4a7b3dafc8ac37801&locations=333f5ad2433c1000f78f820bceb80000");
        COMPANY_SITES.put("PwC", "https://pwc.wd3.myworkdayjobs.com/Global_Experienced_Careers?q=JAVA&locations=e57e6863118d01cf86b0d379342bfab4");
        COMPANY_SITES.put("Deutsche Bank", "https://db.wd3.myworkdayjobs.com/en-US/DBWebsite?q=java&shared_id=YTI2Zjc0OWEtNGU0OC00NGE1LTlhZDMtMzE1OGZjM2JmMjZh&Country=c4f78be1a8f14da0ab49ce1162348a5e&jobFamilyGroup=645e861bc53a0168c673f8fe073b20a8&Location=b3df871f1f9d01ccaebc2b17a8409644&Location=b3df871f1f9d014784a8771aa840824c");
        COMPANY_SITES.put("Western Union", "https://westernunion.wd1.myworkdayjobs.com/en-US/WesternUnionCareers?q=java");
        COMPANY_SITES.put("Master Card", "https://mastercard.wd1.myworkdayjobs.com/en-US/CorporateCareers?q=java&locations=8eab563831bf10acbc722e4859721571&jobFamilyGroup=866c0ed135ff106f00587685e7483440");
    }

    public static List<Map<String, String>> getJobsFromSite(String company, String url) {
        List<Map<String, String>> jobs = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        // Set implicit wait
        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));


            long lastHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
            for (int i = 0; i < 5; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000);

                long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) break;
                lastHeight = newHeight;
            }

            List<WebElement> jobElements = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a[data-automation-id='jobTitle']"))
            );

            // Fallback to a different selector if the first one doesn't find any jobs
            if (jobElements.isEmpty()) {
                jobElements = driver.findElements(By.cssSelector("div[data-automation-id='jobCard'] a"));
            }

            for (WebElement job : jobElements) {
                String title = job.getText().trim();
                String link = job.getAttribute("href");

                Map<String, String> jobData = new HashMap<>();
                jobData.put("company", company);
                jobData.put("title", title);
                jobData.put("link", link);

                jobs.add(jobData);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error fetching jobs from " + company + ": " + e.getMessage());
        } finally {
            driver.quit();
        }

        return jobs;
    }

    //main method to run the scraper
    public static void main(String[] args) {
        List<Map<String, String>> allJobs = new ArrayList<>();

        for (Map.Entry<String, String> entry : COMPANY_SITES.entrySet()) {
            String company = entry.getKey();
            String url = entry.getValue();

            List<Map<String, String>> jobs = getJobsFromSite(company, url);

            if (!jobs.isEmpty()) {
                System.out.println("\n✅ Jobs found at " + company + ":");
                for (Map<String, String> job : jobs) {
                    System.out.println(job.get("title") + " - " + job.get("link"));
                }
                allJobs.addAll(jobs);
            } else {
                System.out.println("\n❌ No jobs found at " + company);
            }
        }
    }
}
