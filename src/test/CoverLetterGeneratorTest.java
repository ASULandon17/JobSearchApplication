package com.jobsearch.coverletter;

import com.jobsearch.model.JobPosting;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;

public class CoverLetterGeneratorTest {
    
    private CoverLetterGenerator generator;
    private JobPosting mockJob;
    
    @Before
    public void setUp() {
        generator = new CoverLetterGenerator();
        
        mockJob = new JobPosting();
        mockJob.setTitle("Software Engineer");
        mockJob.setCompany("Tech Corp");
        mockJob.setDescription("We are looking for a Software Engineer with experience in " +
            "Java, Python, and React. The ideal candidate has 3-5 years of experience " +
            "and strong communication skills. Responsibilities include developing features, " +
            "code review, and mentoring junior developers.");
        mockJob.setLocation("San Francisco, CA");
        mockJob.setUrl("https://example.com/job");
    }
    
    @Test
    public void testCoverLetterContentComplete() {
        CoverLetterContent content = new CoverLetterContent();
        content.setOpening("Test opening");
        content.addBodyParagraph("Test paragraph");
        content.setClosing("Test closing");
        
        assertTrue(content.isComplete());
    }
    
    @Test
    public void testSkillMatching() {
        SkillMatchResult result = new SkillMatchResult();
        result.addMatchedSkill("Java", 3);
        result.addMatchedSkill("Python", 2);
        result.addUnmatchedSkill("Go");
        
        assertEquals(2, result.getMatchedSkillsCount());
        assertEquals(1, result.getUnmatchedSkillsCount());
    }
    
    @Test
    public void testMatchScore() {
        SkillMatchResult result = new SkillMatchResult();
        result.setComponentScores(0.8, 0.7, 0.9);
        
        double expected = (0.8 * 0.5) + (0.7 * 0.2) + (0.9 * 0.3);
        assertEquals(expected, result.getOverallMatchScore(), 0.001);
    }
    
    @Test
    public void testResumeDataSkills() {
        ResumeData resume = new ResumeData();
        resume.addSkill("Java", 5);
        resume.addSkill("Python", 3);
        
        assertTrue(resume.hasSkill("Java"));
        assertFalse(resume.hasSkill("Go"));
        assertEquals(5, resume.getSkillExperience("Java"));
    }
    
    @Test
    public void testWordCount() {
        String text = "This is a test sentence with ten words in total";
        assertEquals(10, CoverLetterUtil.countWords(text));
    }
    
    @Test
    public void testSkillSimilarity() {
        double similarity = CoverLetterUtil.calculateSimilarity("Java", "Javascript");
        assertTrue(similarity > 0.5); // Should be somewhat similar
    }
}