package com.jobsearch.coverletter;

import com.jobsearch.model.JobPosting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CoverLetterTemplateEngine {
    private static final Logger logger = LoggerFactory.getLogger(CoverLetterTemplateEngine.class);
    
    public String generate(CoverLetterContent content, 
                          JobPosting job,
                          ResumeData resumeData,
                          CompanyInfo companyInfo) {
        
        StringBuilder coverLetter = new StringBuilder();
        
        // Header with contact information
        coverLetter.append(generateHeader(resumeData));
        coverLetter.append("\n\n");
        
        // Date
        coverLetter.append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        coverLetter.append("\n\n");
        
        // Recipient information
        coverLetter.append(generateRecipient(job, companyInfo));
        coverLetter.append("\n\n");
        
        // Salutation
        coverLetter.append("Dear Hiring Manager,\n\n");
        
        // Opening paragraph
        coverLetter.append(content.getOpening());
        coverLetter.append("\n\n");
        
        // Body paragraphs
        for (String paragraph : content.getBodyParagraphs()) {
            coverLetter.append(paragraph);
            coverLetter.append("\n\n");
        }
        
        // Closing paragraph
        coverLetter.append(content.getClosing());
        coverLetter.append("\n\n");
        
        // Sign-off
        coverLetter.append("Sincerely,\n");
        coverLetter.append(resumeData.getName());
        
        logger.info("Generated formatted cover letter");
        return coverLetter.toString();
    }
    
    public String generateBasic(JobPosting job, ResumeData resumeData) {
        StringBuilder coverLetter = new StringBuilder();
        
        coverLetter.append(generateHeader(resumeData));
        coverLetter.append("\n\n");
        coverLetter.append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        coverLetter.append("\n\n");
        
        coverLetter.append("Dear Hiring Manager,\n\n");
        
        coverLetter.append(String.format(
            "I am writing to express my interest in the %s position at %s. ",
            job.getTitle(), job.getCompany()));
        
        coverLetter.append("My background and skills align well with the requirements for this role.\n\n");
        
        coverLetter.append("I am excited about the opportunity to contribute to your team ");
        coverLetter.append("and would welcome the chance to discuss my qualifications further.\n\n");
        
        coverLetter.append("Thank you for your consideration.\n\n");
        coverLetter.append("Sincerely,\n");
        coverLetter.append(resumeData.getName());
        
        return coverLetter.toString();
    }
    
    private String generateHeader(ResumeData resumeData) {
        StringBuilder header = new StringBuilder();
        
        if (resumeData.getName() != null) {
            header.append(resumeData.getName()).append("\n");
        }
        
        if (resumeData.getEmail() != null) {
            header.append(resumeData.getEmail()).append("\n");
        }
        
        if (resumeData.getPhone() != null) {
            header.append(resumeData.getPhone()).append("\n");
        }
        
        if (resumeData.getLinkedIn() != null) {
            header.append(resumeData.getLinkedIn()).append("\n");
        }
        
        return header.toString();
    }
    
    private String generateRecipient(JobPosting job, CompanyInfo companyInfo) {
        StringBuilder recipient = new StringBuilder();
        
        recipient.append("Hiring Manager\n");
        recipient.append(companyInfo.getName()).append("\n");
        
        if (job.getLocation() != null && !job.getLocation().equals("Remote")) {
            recipient.append(job.getLocation()).append("\n");
        }
        
        return recipient.toString();
    }
}