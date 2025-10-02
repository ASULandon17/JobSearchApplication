package com.jobsearch.utils;

import com.jobsearch.model.JobPosting;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    public static void export(List<JobPosting> jobs, File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Job Search Results");
        
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        String[] headers = {"Job Title", "Company", "Location", "Salary", 
                           "Posted Date", "Relevance Score", "Reputation Score", 
                           "URL", "Source"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (JobPosting job : jobs) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(job.getTitle());
            row.createCell(1).setCellValue(job.getCompany());
            row.createCell(2).setCellValue(job.getLocation() != null ? 
                job.getLocation() : "N/A");
            row.createCell(3).setCellValue(job.getSalary() != null ? 
                job.getSalary() : "N/A");
            
            Cell dateCell = row.createCell(4);
            if (job.getPostedDate() != null) {
                dateCell.setCellValue(job.getPostedDate().format(DATE_FORMATTER));
            } else {
                dateCell.setCellValue("N/A");
            }
            
            row.createCell(5).setCellValue(job.getRelevanceScore());
            row.createCell(6).setCellValue(job.getReputabilityScore());
            row.createCell(7).setCellValue(job.getUrl());
            row.createCell(8).setCellValue(job.getSource());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        
        workbook.close();
    }
}