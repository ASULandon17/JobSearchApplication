package com.jobsearch.nlp;

import com.jobsearch.coverletter.ResumeData;
import com.jobsearch.utils.PDFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResumeParser {
    private static final Logger logger = LoggerFactory.getLogger(ResumeParser.class);
    
    public ResumeData parse(File resumeFile) {
        try {
            String text = PDFReader.extractText(resumeFile);
            ResumeData data = new ResumeData();
            
            // Extract contact information
            extractContactInfo(text, data);
            
            // Extract skills
            extractSkills(text, data);
            
            // Extract experience
            extractExperience(text, data);
            
            // Extract education
            extractEducation(text, data);
            
            // Extract projects
            extractProjects(text, data);
            
            logger.info("Successfully parsed resume");
            return data;
            
        } catch (Exception e) {
            logger.error("Error parsing resume: {}", e.getMessage());
            return new ResumeData();
        }
    }
    
    private void extractContactInfo(String text, ResumeData data) {
        // Extract name (usually first line)
        String[] lines = text.split("\\n");
        if (lines.length > 0) {
            data.setName(lines[0].trim());
        }
        
        // Extract email
        Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            data.setEmail(emailMatcher.group());
        }
        
        // Extract phone
        Pattern phonePattern = Pattern.compile(
            "\\(?(\\d{3})\\)?[-.]?(\\d{3})[-.]?(\\d{4})");
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            data.setPhone(phoneMatcher.group());
        }
        
        // Extract LinkedIn
        Pattern linkedinPattern = Pattern.compile(
            "linkedin\\.com/in/[\\w-]+", Pattern.CASE_INSENSITIVE);
        Matcher linkedinMatcher = linkedinPattern.matcher(text);
        if (linkedinMatcher.find()) {
            data.setLinkedIn("https://" + linkedinMatcher.group());
        }
    }
    
    private void extractSkills(String text, ResumeData data) {
        // Find skills section
        Pattern skillsSection = Pattern.compile(
            "(?:SKILLS|TECHNICAL SKILLS|COMPETENCIES)([\\s\\S]*?)(?=\\n[A-Z]{2,}|$)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = skillsSection.matcher(text);
        
        if (matcher.find()) {
            String skillsText = matcher.group(1);
            
            // Split by common delimiters
            String[] skills = skillsText.split("[,;•\n]");
            
            Map<String, Integer> skillsMap = new HashMap<>();
            for (String skill : skills) {
                String cleaned = skill.trim()
                    .replaceAll("^[\\-\\*•]\\s*", "")
                    .toLowerCase();
                
                if (cleaned.length() > 2 && cleaned.length() < 50) {
                    // Estimate experience level (simplified)
                    skillsMap.put(cleaned, 2);
                }
            }
            
            data.setSkills(skillsMap);
        }
    }
    
    private void extractExperience(String text, ResumeData data) {
        // Find experience section
        Pattern expSection = Pattern.compile(
            "(?:EXPERIENCE|WORK EXPERIENCE|EMPLOYMENT)([\\s\\S]*?)(?=\\n[A-Z]{2,}|EDUCATION|$)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = expSection.matcher(text);
        
        List<Map<String, String>> experiences = new ArrayList<>();
        int totalYears = 0;
        
        if (matcher.find()) {
            String expText = matcher.group(1);
            
            // Extract job entries (simplified)
            Pattern jobPattern = Pattern.compile(
                "([\\w\\s]+)\\s+[-|]\\s+([\\w\\s&,]+)\\s+([\\d]{4})\\s*[-–]\\s*([\\d]{4}|Present)",
                Pattern.CASE_INSENSITIVE);
            Matcher jobMatcher = jobPattern.matcher(expText);
            
            while (jobMatcher.find()) {
                Map<String, String> job = new HashMap<>();
                job.put("title", jobMatcher.group(1).trim());
                job.put("company", jobMatcher.group(2).trim());
                job.put("startYear", jobMatcher.group(3));
                job.put("endYear", jobMatcher.group(4));
                
                // Calculate years
                try {
                    int start = Integer.parseInt(jobMatcher.group(3));
                    int end = jobMatcher.group(4).equalsIgnoreCase("Present") 
                        ? Calendar.getInstance().get(Calendar.YEAR)
                        : Integer.parseInt(jobMatcher.group(4));
                    totalYears += (end - start);
                } catch (NumberFormatException e) {
                    // Ignore
                }
                
                experiences.add(job);
            }
        }
        
        data.setExperience(experiences);
        data.setTotalYearsExperience(totalYears);
    }
    
    private void extractEducation(String text, ResumeData data) {
        // Find education section
        Pattern eduSection = Pattern.compile(
            "(?:EDUCATION)([\\s\\S]*?)(?=\\n[A-Z]{2,}|$)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = eduSection.matcher(text);
        
        List<Map<String, String>> education = new ArrayList<>();
        
        if (matcher.find()) {
            String eduText = matcher.group(1);
            
            // Extract degrees
            Pattern degreePattern = Pattern.compile(
                "(Bachelor|Master|PhD|Associate|B\\.S\\.|M\\.S\\.|Ph\\.D\\.)([^\\n]+)",
                Pattern.CASE_INSENSITIVE);
            Matcher degreeMatcher = degreePattern.matcher(eduText);
            
            while (degreeMatcher.find()) {
                Map<String, String> degree = new HashMap<>();
                degree.put("level", degreeMatcher.group(1));
                degree.put("details", degreeMatcher.group(2).trim());
                education.add(degree);
            }
        }
        
        data.setEducation(education);
    }
    
    private void extractProjects(String text, ResumeData data) {
        Pattern projectSection = Pattern.compile(
            "(?:PROJECTS|PERSONAL PROJECTS)([\\s\\S]*?)(?=\\n[A-Z]{2,}|$)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = projectSection.matcher(text);
        
        List<String> projects = new ArrayList<>();
        
        if (matcher.find()) {
            String projectText = matcher.group(1);
            String[] lines = projectText.split("\\n");
            
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.length() > 10 && !trimmed.matches("^[\\s•\\-\\*]+$")) {
                    projects.add(trimmed);
                }
            }
        }
        
        data.setProjects(projects);
    }
}