package com.jobsearch.gui;

import com.jobsearch.model.JobPosting;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JobTableModel extends AbstractTableModel {
    private final String[] columnNames = {
        "Job Title", "Company", "Salary", "Posted Date", 
        "Relevance (0-10)", "Reputation (0-10)", "URL"
    };
    
    private List<JobPosting> jobs = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    public void setJobs(List<JobPosting> jobs) {
        this.jobs = jobs;
        fireTableDataChanged();
    }
    
    public List<JobPosting> getJobs() {
        return jobs;
    }
    
    @Override
    public int getRowCount() {
        return jobs.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        JobPosting job = jobs.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return job.getTitle();
            case 1: return job.getCompany();
            case 2: return job.getSalary() != null ? job.getSalary() : "N/A";
            case 3: return job.getPostedDate() != null ? 
                job.getPostedDate().format(DATE_FORMATTER) : "N/A";
            case 4: return job.getRelevanceScore();
            case 5: return job.getReputabilityScore();
            case 6: return job.getUrl();
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 4 || columnIndex == 5) {
            return Integer.class;
        }
        return String.class;
    }
}