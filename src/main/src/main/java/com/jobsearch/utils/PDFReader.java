package com.jobsearch.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class PDFReader {
    
    public static String extractText(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    public static String[] extractKeywords(File pdfFile) throws IOException {
        String text = extractText(pdfFile);
        // Extract skills and keywords from resume
        // This is a simplified version - you might want to implement
        // more sophisticated NLP techniques
        
        String[] lines = text.split("\\n");
        Set<String> keywords = new HashSet<>();
        
        for (String line : lines) {
            // Look for common sections
            if (line.toLowerCase().contains("skills") ||
                line.toLowerCase().contains("technologies") ||
                line.toLowerCase().contains("experience")) {
                // Extract relevant keywords
                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (word.length() > 3) {
                        keywords.add(word.toLowerCase());
                    }
                }
            }
        }
        
        return keywords.toArray(new String[0]);
    }
}