package com.jobsearch.coverletter;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CoverLetterMLModel {
    private static final Logger logger = LoggerFactory.getLogger(CoverLetterMLModel.class);
    
    private MultiLayerNetwork model;
    private final Map<String, double[]> phraseEmbeddings;
    private final Random random;
    
    public CoverLetterMLModel() {
        this.phraseEmbeddings = new HashMap<>();
        this.random = new Random();
        initializeModel();
        trainModel();
    }
    
    private void initializeModel() {
        int inputSize = 50;  // Feature vector size
        int hiddenSize = 100;
        int outputSize = 20; // Number of template variations
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(inputSize)
                .nOut(hiddenSize)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(hiddenSize)
                .nOut(hiddenSize)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nIn(hiddenSize)
                .nOut(outputSize)
                .activation(Activation.SOFTMAX)
                .build())
            .build();
        
        model = new MultiLayerNetwork(conf);
        model.init();
        
        logger.info("ML model initialized");
    }
    
    private void trainModel() {
        // In a real implementation, this would train on a dataset of successful cover letters
        // For now, we'll use a pre-configured model
        logger.info("ML model ready for inference");
    }
    
    public CoverLetterContent generateContent(JobRequirements requirements,
                                             ResumeData resumeData,
                                             CompanyInfo companyInfo,
                                             SkillMatchResult matchResult) {
        
        CoverLetterContent content = new CoverLetterContent();
        
        // Generate opening paragraph
        String opening = generateOpening(requirements, companyInfo, matchResult);
        content.setOpening(opening);
        
        // Generate body paragraphs highlighting relevant experience
        List<String> bodyParagraphs = generateBodyParagraphs(
            requirements, resumeData, matchResult);
        content.setBodyParagraphs(bodyParagraphs);
        
        // Generate closing paragraph
        String closing = generateClosing(companyInfo);
        content.setClosing(closing);
        
        // Generate specific achievements to highlight
        List<String> achievements = selectRelevantAchievements(
            requirements, resumeData, matchResult);
        content.setAchievements(achievements);
        
        return content;
    }
    
    private String generateOpening(JobRequirements requirements,
                                   CompanyInfo companyInfo,
                                   SkillMatchResult matchResult) {
        
        List<String> openingTemplates = Arrays.asList(
            "I am writing to express my strong interest in the %s position at %s. With my background in %s and proven experience in %s, I am confident I can contribute significantly to your team.",
            
            "I am excited to apply for the %s role at %s. %s has established itself as %s, and I am eager to bring my expertise in %s to support your continued success.",
            
            "As a passionate professional with extensive experience in %s, I was thrilled to discover the %s opportunity at %s. My skills in %s align perfectly with your requirements.",
            
            "I am writing to apply for the %s position at %s. Your company's commitment to %s resonates strongly with my professional values, and my experience in %s makes me an ideal candidate."
        );
        
        // Use ML model to select best template based on match score
        int templateIndex = selectBestTemplate(matchResult.getOverallMatchScore(), 
                                              openingTemplates.size());
        String template = openingTemplates.get(templateIndex);
        
        // Fill in template
        String jobTitle = requirements.getJobTitle() != null ? 
            requirements.getJobTitle() : "position";
        String companyName = companyInfo.getName();
        
        List<String> topSkills = matchResult.getMatchedSkills().stream()
            .limit(3)
            .collect(Collectors.toList());
        
        String skillsString = String.join(", ", topSkills);
        
        return String.format(template,
            jobTitle,
            companyName,
            companyInfo.getIndustry(),
            skillsString,
            "innovation and excellence"
        );
    }
    
    private List<String> generateBodyParagraphs(JobRequirements requirements,
                                               ResumeData resumeData,
                                               SkillMatchResult matchResult) {
        
        List<String> paragraphs = new ArrayList<>();
        
        // Paragraph 1: Technical skills and experience
        StringBuilder techPara = new StringBuilder();
        techPara.append("In my previous roles, I have developed strong expertise in ");
        
        List<String> matchedSkills = matchResult.getMatchedSkills();
        if (!matchedSkills.isEmpty()) {
            techPara.append(String.join(", ", matchedSkills.subList(0, 
                Math.min(5, matchedSkills.size()))));
            techPara.append(". ");
        }
        
        if (resumeData.getTotalYearsExperience() > 0) {
            techPara.append(String.format("With %d years of professional experience, ",
                resumeData.getTotalYearsExperience()));
            techPara.append("I have successfully delivered complex projects and ");
            techPara.append("collaborated with cross-functional teams to achieve exceptional results.");
        }
        
        paragraphs.add(techPara.toString());
        
        // Paragraph 2: Specific achievements
        if (!resumeData.getExperience().isEmpty()) {
            StringBuilder achievementPara = new StringBuilder();
            achievementPara.append("At my most recent position at ");
            
            Map<String, String> latestJob = resumeData.getExperience().get(0);
            achievementPara.append(latestJob.get("company"));
            achievementPara.append(", I ");
            
            // Generate achievement-focused content
            achievementPara.append("led key initiatives that improved system performance, ");
            achievementPara.append("enhanced user experience, and contributed to the company's growth. ");
            achievementPara.append("My ability to quickly adapt to new technologies and ");
            achievementPara.append("solve complex problems has been instrumental in delivering high-quality solutions.");
            
            paragraphs.add(achievementPara.toString());
        }
        
        // Paragraph 3: Soft skills and culture fit
        StringBuilder softSkillsPara = new StringBuilder();
        softSkillsPara.append("Beyond technical proficiency, I pride myself on my ");
        
        List<String> softSkills = matchResult.getMatchedSoftSkills();
        if (!softSkills.isEmpty()) {
            softSkillsPara.append(String.join(", ", softSkills));
            softSkillsPara.append(". ");
        } else {
            softSkillsPara.append("strong communication skills, collaborative mindset, and problem-solving abilities. ");
        }
        
        softSkillsPara.append("I thrive in dynamic environments where I can learn, innovate, and ");
        softSkillsPara.append("contribute to meaningful projects that make a difference.");
        
        paragraphs.add(softSkillsPara.toString());
        
        return paragraphs;
    }
    
    private String generateClosing(CompanyInfo companyInfo) {
        List<String> closingTemplates = Arrays.asList(
            "I am enthusiastic about the opportunity to contribute to %s's success and would welcome the chance to discuss how my skills and experience align with your needs. Thank you for considering my application.",
            
            "I am excited about the possibility of joining %s and contributing to your innovative projects. I look forward to discussing how I can add value to your team.",
            
            "Thank you for considering my application. I am eager to bring my passion and expertise to %s and would appreciate the opportunity to discuss this role further.",
            
            "I am confident that my background and skills make me a strong candidate for this position. I look forward to the opportunity to discuss how I can contribute to %s's continued success."
        );
        
        int index = random.nextInt(closingTemplates.size());
        return String.format(closingTemplates.get(index), companyInfo.getName());
    }
    
    private List<String> selectRelevantAchievements(JobRequirements requirements,
                                                   ResumeData resumeData,
                                                   SkillMatchResult matchResult) {
        
        List<String> achievements = new ArrayList<>();
        
        // Generate achievement bullets based on matched skills
        for (String skill : matchResult.getMatchedSkills()) {
            String achievement = String.format(
                "Demonstrated proficiency in %s through successful project delivery and problem-solving",
                skill
            );
            achievements.add(achievement);
            
            if (achievements.size() >= 3) break;
        }
        
        return achievements;
    }
    
    private int selectBestTemplate(double matchScore, int numTemplates) {
        // Use match score to influence template selection
        INDArray input = Nd4j.create(new double[]{matchScore});
        // In real implementation, would use model.output(input)
        
        // For now, use simple logic
        if (matchScore > 0.8) {
            return 0; // Most confident template
        } else if (matchScore > 0.6) {
            return 1;
        } else {
            return 2;
        }
    }
}