package com.jobsearch.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Utility class for loading OpenNLP models from resources
 */
public class NLPModelLoader {
    private static final Logger logger = LoggerFactory.getLogger(NLPModelLoader.class);
    
    private static final String MODELS_PATH = "/models/";
    
    /**
     * Load a model file from resources
     * @param modelName Name of the model file (e.g., "en-sent.bin")
     * @return InputStream of the model
     * @throws RuntimeException if model cannot be loaded
     */
    public static InputStream loadModel(String modelName) {
        try {
            String resourcePath = MODELS_PATH + modelName;
            InputStream stream = NLPModelLoader.class.getResourceAsStream(resourcePath);
            
            if (stream == null) {
                // Try alternative paths
                stream = ClassLoader.getSystemResourceAsStream(resourcePath);
            }
            
            if (stream == null) {
                // Try from file system
                String filePath = "src/main/resources" + resourcePath;
                try {
                    stream = new java.io.FileInputStream(filePath);
                    logger.info("Loaded model from file system: {}", filePath);
                } catch (Exception e) {
                    logger.error("Could not find model: {}", modelName);
                    throw new RuntimeException("Model not found: " + modelName);
                }
            } else {
                logger.info("Loaded model from resources: {}", modelName);
            }
            
            return stream;
            
        } catch (Exception e) {
            logger.error("Error loading model {}: {}", modelName, e.getMessage());
            throw new RuntimeException("Failed to load NLP model: " + modelName, e);
        }
    }
    
    /**
     * Check if all required models are available
     * @return true if all models are available
     */
    public static boolean allModelsAvailable() {
        String[] models = {"en-sent.bin", "en-token.bin", "en-pos-maxent.bin"};
        
        for (String model : models) {
            try {
                InputStream stream = loadModel(model);
                if (stream != null) {
                    stream.close();
                } else {
                    logger.warn("Model not available: {}", model);
                    return false;
                }
            } catch (Exception e) {
                logger.warn("Error checking model {}: {}", model, e.getMessage());
                return false;
            }
        }
        
        logger.info("All NLP models are available");
        return true;
    }
}