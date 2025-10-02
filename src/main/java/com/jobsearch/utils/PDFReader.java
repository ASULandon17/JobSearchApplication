package com.jobsearch.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PDFReader {
    
    public static String extractText(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    public static String[] extractKeywords(File pdfFile) throws IOException {
        String text = extractText(pdfFile);
        
        String[] lines = text.split("\\n");
        Set<String> keywords = new HashSet<>();
        
        for (String line : lines) {
            if (line.toLowerCase().contains("skills") ||
                line.toLowerCase().contains("technologies") ||
                line.toLowerCase().contains("experience")) {
                
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