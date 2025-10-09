package com.jobsearch.coverletter;

import com.jobsearch.model.JobPosting;
import com.jobsearch.nlp.JobRequirementsExtractor;
import com.jobsearch.nlp.ResumeParser;
import com.jobsearch.nlp.CompanyResearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CoverLetterGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CoverLetterGenerator.class);
    
    private final JobRequirementsExtractor requirementsExtractor;
    private final ResumeParser resumeParser;
    private final CompanyResearcher companyResearcher;
    private final CoverLetterTemplateEngine templateEngine;
    private final CoverLetterMLModel mlModel;
    
    public CoverLetterGenerator() {
        this.requirementsExtractor = new JobRequirementsExtractor();
        this.resumeParser = new ResumeParser();
        this.companyResearcher = new CompanyResearcher();
        this.templateEngine = new CoverLetterTemplateEngine();
        this.mlModel = new CoverLetterMLModel();
        
        logger.info("CoverLetterGenerator initialized");
    }
    
    public String generateCoverLetter(JobPosting job, File resumeFile) {
        try {
            logger.info("Generating cover letter for: {} at {}", 
                job.getTitle(), job.getCompany());
            
            // Step 1: Extract job requirements
            JobRequirements requirements = requirementsExtractor.extract(job);
            
            // Step 2: Parse resume
            ResumeData resumeData = resumeParser.parse(resumeFile);
            
            // Step 3: Research company
            CompanyInfo companyInfo = companyResearcher.research(
                job.getCompany(), job.getUrl());
            
            // Step 4: Match skills and experience
            SkillMatchResult matchResult = matchSkillsAndExperience(
                requirements, resumeData);
            
            // Step 5: Generate personalized content using ML model
            CoverLetterContent content = mlModel.generateContent(
                requirements, resumeData, companyInfo, matchResult);
            
            // Step 6: Apply template and formatting
            String coverLetter = templateEngine.generate(
                content, job, resumeData, companyInfo);
            
            logger.info("Successfully generated cover letter");
            return coverLetter;
            
        } catch (Exception e) {
            logger.error("Error generating cover letter: {}", e.getMessage());
            return generateFallbackCoverLetter(job, resumeFile);
        }
    }
    
    private SkillMatchResult matchSkillsAndExperience(
            JobRequirements requirements, ResumeData resumeData) {
        
        SkillMatchResult result = new SkillMatchResult();
        
        // Match technical skills
        for (String requiredSkill : requirements.getTechnicalSkills()) {
            if (resumeData.hasSkill(requiredSkill)) {
                result.addMatchedSkill(requiredSkill, 
                    resumeData.getSkillExperience(requiredSkill));
            }
        }
        
        // Match soft skills
        for (String softSkill : requirements.getSoftSkills()) {
            if (resumeData.hasSoftSkill(softSkill)) {
                result.addMatchedSoftSkill(softSkill);
            }
        }
        
        // Match experience level
        result.setExperienceMatch(
            requirements.getYearsExperience() <= resumeData.getTotalYearsExperience());
        
        // Calculate overall match score
        double matchScore = calculateMatchScore(requirements, resumeData, result);
        result.setOverallMatchScore(matchScore);
        
        return result;
    }
    
    private double calculateMatchScore(JobRequirements requirements, 
                                      ResumeData resumeData, 
                                      SkillMatchResult result) {
        double skillScore = (double) result.getMatchedSkillsCount() / 
            Math.max(1, requirements.getTechnicalSkills().size());
        
        double experienceScore = result.isExperienceMatch() ? 1.0 : 0.5;
        
        double educationScore = requirements.getEducationLevel() != null &&
            resumeData.meetsEducationRequirement(requirements.getEducationLevel()) 
            ? 1.0 : 0.7;
        
        return (skillScore * 0.5) + (experienceScore * 0.3) + (educationScore * 0.2);
    }
    
    private String generateFallbackCoverLetter(JobPosting job, File resumeFile) {
        try {
            ResumeData resumeData = resumeParser.parse(resumeFile);
            return templateEngine.generateBasic(job, resumeData);
        } catch (Exception e) {
            logger.error("Fallback generation failed: {}", e.getMessage());
            return "Error generating cover letter. Please try again.";
        }
    }
}