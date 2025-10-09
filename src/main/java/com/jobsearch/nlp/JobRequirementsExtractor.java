package com.jobsearch.nlp;

import com.jobsearch.model.JobPosting;
import com.jobsearch.utils.NLPModelLoader;
import com.jobsearch.coverletter.JobRequirements;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobRequirementsExtractor {
    private static final Logger logger = LoggerFactory.getLogger(JobRequirementsExtractor.class);
    
    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    
    // Common technical skills dictionary
    private static final Set<String> TECHNICAL_SKILLS = new HashSet<>(Arrays.asList(
        "java", "python", "javascript", "typescript", "c++", "c#", "ruby", "php",
        "react", "angular", "vue", "node.js", "spring", "django", "flask",
        "sql", "nosql", "mongodb", "postgresql", "mysql", "redis",
        "aws", "azure", "gcp", "docker", "kubernetes", "jenkins",
        "git", "agile", "scrum", "rest", "api", "microservices",
        "machine learning", "deep learning", "tensorflow", "pytorch",
        "html", "css", "sass", "webpack", "babel", "npm"
    ));
    
    private static final Set<String> SOFT_SKILLS = new HashSet<>(Arrays.asList(
        "communication", "leadership", "teamwork", "problem solving",
        "analytical", "creative", "collaborative", "self-motivated",
        "detail-oriented", "time management", "adaptable", "innovative"
    ));
    
    public JobRequirementsExtractor() {
        try {
            // Check if models are available
            if (!NLPModelLoader.allModelsAvailable()) {
                logger.warn("Not all NLP models available, will use limited extraction");
            }
            
            // Load OpenNLP models
            try (InputStream sentenceModelIn = NLPModelLoader.loadModel("en-sent.bin")) {
                SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
                this.sentenceDetector = new SentenceDetectorME(sentenceModel);
                logger.info("Sentence detector loaded");
            }
            
            try (InputStream tokenModelIn = NLPModelLoader.loadModel("en-token.bin")) {
                TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
                this.tokenizer = new TokenizerME(tokenModel);
                logger.info("Tokenizer loaded");
            }
            
            try (InputStream posModelIn = NLPModelLoader.loadModel("en-pos-maxent.bin")) {
                POSModel posModel = new POSModel(posModelIn);
                this.posTagger = new POSTaggerME(posModel);
                logger.info("POS tagger loaded");
            }
            
            logger.info("All NLP models loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading NLP models: {}", e.getMessage());
            logger.warn("Cover letter generation will use fallback methods");
        }
    }
    
    public JobRequirements extract(JobPosting job) {
        JobRequirements requirements = new JobRequirements();
        
        String description = job.getDescription();
        if (description == null || description.isEmpty()) {
            logger.warn("No description available for job: {}", job.getTitle());
            return extractFromTitle(job, requirements);
        }
        
        // Detect sentences
        String[] sentences = sentenceDetector.sentDetect(description);
        
        // Extract technical skills
        Set<String> foundSkills = extractTechnicalSkills(description);
        requirements.setTechnicalSkills(new ArrayList<>(foundSkills));
        
        // Extract soft skills
        Set<String> foundSoftSkills = extractSoftSkills(description);
        requirements.setSoftSkills(new ArrayList<>(foundSoftSkills));
        
        // Extract years of experience
        int years = extractYearsExperience(description);
        requirements.setYearsExperience(years);
        
        // Extract education requirements
        String education = extractEducation(description);
        requirements.setEducationLevel(education);
        
        // Extract responsibilities
        List<String> responsibilities = extractResponsibilities(sentences);
        requirements.setResponsibilities(responsibilities);
        
        // Extract qualifications
        List<String> qualifications = extractQualifications(sentences);
        requirements.setQualifications(qualifications);
        
        // Extract key phrases
        List<String> keyPhrases = extractKeyPhrases(description);
        requirements.setKeyPhrases(keyPhrases);
        
        logger.debug("Extracted {} skills, {} responsibilities from job description",
            foundSkills.size(), responsibilities.size());
        
        return requirements;
    }
    
    private Set<String> extractTechnicalSkills(String text) {
        Set<String> foundSkills = new HashSet<>();
        String lowerText = text.toLowerCase();
        
        for (String skill : TECHNICAL_SKILLS) {
            // Use word boundaries to avoid partial matches
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b",
                Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                foundSkills.add(skill);
            }
        }
        
        // Extract additional skills using patterns
        Pattern skillPattern = Pattern.compile(
            "(?:experience with|proficient in|knowledge of|familiar with)\\s+([\\w\\s,]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = skillPattern.matcher(text);
        
        while (matcher.find()) {
            String skillsText = matcher.group(1);
            String[] skills = skillsText.split(",");
            for (String skill : skills) {
                String trimmed = skill.trim().toLowerCase();
                if (trimmed.length() > 2 && trimmed.length() < 30) {
                    foundSkills.add(trimmed);
                }
            }
        }
        
        return foundSkills;
    }
    
    private Set<String> extractSoftSkills(String text) {
        Set<String> foundSkills = new HashSet<>();
        String lowerText = text.toLowerCase();
        
        for (String skill : SOFT_SKILLS) {
            if (lowerText.contains(skill)) {
                foundSkills.add(skill);
            }
        }
        
        return foundSkills;
    }
    
    private int extractYearsExperience(String text) {
        // Patterns like "3+ years", "5-7 years", "minimum 4 years"
        Pattern pattern = Pattern.compile(
            "(\\d+)(?:\\+|\\s*-\\s*\\d+)?\\s*(?:years?|yrs?)\\s+(?:of\\s+)?experience",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        return 0;
    }
    
    private String extractEducation(String text) {
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("phd") || lowerText.contains("doctorate")) {
            return "PhD";
        } else if (lowerText.contains("master") || lowerText.contains("ms") || 
                   lowerText.contains("m.s.")) {
            return "Master's";
        } else if (lowerText.contains("bachelor") || lowerText.contains("bs") || 
                   lowerText.contains("b.s.") || lowerText.contains("ba")) {
            return "Bachelor's";
        } else if (lowerText.contains("associate")) {
            return "Associate's";
        }
        
        return "Not specified";
    }
    
    private List<String> extractResponsibilities(String[] sentences) {
        List<String> responsibilities = new ArrayList<>();
        
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            if (lower.contains("will") || lower.contains("responsible for") ||
                lower.contains("duties include") || lower.startsWith("•") ||
                lower.matches("^\\s*-\\s+.*")) {
                
                String cleaned = sentence.replaceAll("^[•\\-\\*]\\s*", "").trim();
                if (cleaned.length() > 20 && cleaned.length() < 200) {
                    responsibilities.add(cleaned);
                }
            }
        }
        
        return responsibilities;
    }
    
    private List<String> extractQualifications(String[] sentences) {
        List<String> qualifications = new ArrayList<>();
        
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            if (lower.contains("required") || lower.contains("must have") ||
                lower.contains("qualification") || lower.contains("requirement")) {
                
                String cleaned = sentence.replaceAll("^[•\\-\\*]\\s*", "").trim();
                if (cleaned.length() > 20 && cleaned.length() < 200) {
                    qualifications.add(cleaned);
                }
            }
        }
        
        return qualifications;
    }
    
    private List<String> extractKeyPhrases(String text) {
        List<String> keyPhrases = new ArrayList<>();
        
        // Tokenize and POS tag
        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);
        
        // Extract noun phrases (simplified)
        StringBuilder currentPhrase = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (tags[i].startsWith("NN") || tags[i].startsWith("JJ")) {
                if (currentPhrase.length() > 0) {
                    currentPhrase.append(" ");
                }
                currentPhrase.append(tokens[i]);
            } else if (currentPhrase.length() > 0) {
                String phrase = currentPhrase.toString().trim();
                if (phrase.split(" ").length >= 2 && phrase.length() > 5) {
                    keyPhrases.add(phrase);
                }
                currentPhrase = new StringBuilder();
            }
        }
        
        return keyPhrases.stream()
            .distinct()
            .limit(10)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private JobRequirements extractFromTitle(JobPosting job, JobRequirements requirements) {
        // Fallback: extract what we can from title
        String title = job.getTitle().toLowerCase();
        
        Set<String> foundSkills = new HashSet<>();
        for (String skill : TECHNICAL_SKILLS) {
            if (title.contains(skill)) {
                foundSkills.add(skill);
            }
        }
        requirements.setTechnicalSkills(new ArrayList<>(foundSkills));
        
        // Infer experience level from title
        if (title.contains("senior") || title.contains("lead")) {
            requirements.setYearsExperience(5);
        } else if (title.contains("junior") || title.contains("entry")) {
            requirements.setYearsExperience(0);
        } else {
            requirements.setYearsExperience(2);
        }
        
        return requirements;
    }
}