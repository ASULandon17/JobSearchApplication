package com.jobsearch.gui;

import com.jobsearch.model.JobPosting;
import com.jobsearch.model.SearchFilters;
import com.jobsearch.model.SearchFilters.WorkModel;
import com.jobsearch.model.SearchFilters.ExperienceLevel;
import com.jobsearch.scraper.WebScraper;
import com.jobsearch.utils.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {
    private JTextField searchField;
    private JButton selectResumeButton;
    private JButton searchButton;
    private JButton exportButton;
    private JTable jobTable;
    private JobTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel resumeLabel;
    
    // New filter components
    private ButtonGroup workModelGroup;
    private JRadioButton remoteRadio, hybridRadio, inPersonRadio, noWorkPrefRadio;
    private JTextField cityField;
    private JTextField stateField;
    private ButtonGroup experienceLevelGroup;
    private JRadioButton juniorRadio, midLevelRadio, seniorRadio, noExpPrefRadio;
    
    private File selectedResume;
    private List<JobPosting> currentJobs;
    
    public MainFrame() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Job Search Assistant - Enhanced");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        setSize(1400, 800);
        setLocationRelativeTo(null);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        int row = 0;
        
        // Resume selection
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Resume:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        resumeLabel = new JLabel("No file selected");
        resumeLabel.setForeground(Color.GRAY);
        panel.add(resumeLabel, gbc);
        
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        selectResumeButton = new JButton("Select Resume");
        selectResumeButton.addActionListener(e -> selectResume());
        panel.add(selectResumeButton, gbc);
        
        row++;
        
        // Search terms
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Search Terms:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        searchField = new JTextField();
        searchField.setToolTipText("Enter job search terms (e.g., 'software engineer', 'data analyst')");
        panel.add(searchField, gbc);
        
        row++;
        
        // Work Model
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Work Model:"), gbc);
        
        JPanel workModelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        workModelGroup = new ButtonGroup();
        
        noWorkPrefRadio = new JRadioButton("No Preference");
        noWorkPrefRadio.setSelected(true);
        remoteRadio = new JRadioButton("Remote");
        hybridRadio = new JRadioButton("Hybrid");
        inPersonRadio = new JRadioButton("In-Person");
        
        workModelGroup.add(noWorkPrefRadio);
        workModelGroup.add(remoteRadio);
        workModelGroup.add(hybridRadio);
        workModelGroup.add(inPersonRadio);
        
        workModelPanel.add(noWorkPrefRadio);
        workModelPanel.add(remoteRadio);
        workModelPanel.add(hybridRadio);
        workModelPanel.add(inPersonRadio);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(workModelPanel, gbc);
        
        row++;
        
        // Location
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Location (Optional):"), gbc);
        
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(new JLabel("City:"));
        cityField = new JTextField(15);
        cityField.setToolTipText("Enter city name");
        locationPanel.add(cityField);
        
        locationPanel.add(Box.createHorizontalStrut(10));
        locationPanel.add(new JLabel("State:"));
        stateField = new JTextField(5);
        stateField.setToolTipText("Enter state code (e.g., CA, NY)");
        locationPanel.add(stateField);
        
        locationPanel.add(Box.createHorizontalStrut(5));
        JLabel radiusLabel = new JLabel("(50 mile radius)");
        radiusLabel.setForeground(Color.GRAY);
        radiusLabel.setFont(radiusLabel.getFont().deriveFont(Font.ITALIC, 11f));
        locationPanel.add(radiusLabel);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(locationPanel, gbc);
        
        row++;
        
        // Experience Level
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Experience Level:"), gbc);
        
        JPanel expPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        experienceLevelGroup = new ButtonGroup();
        
        noExpPrefRadio = new JRadioButton("No Preference");
        noExpPrefRadio.setSelected(true);
        juniorRadio = new JRadioButton("Junior");
        midLevelRadio = new JRadioButton("Mid-Level");
        seniorRadio = new JRadioButton("Senior");
        
        experienceLevelGroup.add(noExpPrefRadio);
        experienceLevelGroup.add(juniorRadio);
        experienceLevelGroup.add(midLevelRadio);
        experienceLevelGroup.add(seniorRadio);
        
        expPanel.add(noExpPrefRadio);
        expPanel.add(juniorRadio);
        expPanel.add(midLevelRadio);
        expPanel.add(seniorRadio);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(expPanel, gbc);
        
        row++;
        
        // Action buttons
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchButton = new JButton("🔍 Search Jobs");
        searchButton.addActionListener(e -> searchJobs());
        searchButton.setEnabled(false);
        searchButton.setFont(searchButton.getFont().deriveFont(Font.BOLD, 14f));
        buttonPanel.add(searchButton);
        
        exportButton = new JButton("📊 Export to Excel");
        exportButton.addActionListener(e -> exportToExcel());
        exportButton.setEnabled(false);
        buttonPanel.add(exportButton);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JScrollPane createTablePanel() {
        tableModel = new JobTableModel();
        jobTable = new JTable(tableModel);
        jobTable.setRowHeight(25);
        jobTable.setAutoCreateRowSorter(true);
        
        jobTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        jobTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        jobTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        jobTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        jobTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        jobTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        jobTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        jobTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        jobTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        jobTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jobTable.rowAtPoint(e.getPoint());
                int col = jobTable.columnAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    try {
                        String url = (String) jobTable.getValueAt(row, col);
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this, 
                            "Could not open URL: " + ex.getMessage());
                    }
                }
            }
        });
        
        return new JScrollPane(jobTable);
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Ready - Select a resume to begin");
        panel.add(statusLabel, BorderLayout.WEST);
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void selectResume() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PDF Files", "pdf"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedResume = fileChooser.getSelectedFile();
            resumeLabel.setText(selectedResume.getName());
            resumeLabel.setForeground(Color.BLACK);
            searchButton.setEnabled(true);
            statusLabel.setText("Resume loaded: " + selectedResume.getName());
        }
    }
    
    private SearchFilters getFiltersFromUI() {
        SearchFilters filters = new SearchFilters();
        filters.setSearchTerms(searchField.getText().trim());
        
        // Work Model
        if (remoteRadio.isSelected()) {
            filters.setWorkModel(WorkModel.REMOTE);
        } else if (hybridRadio.isSelected()) {
            filters.setWorkModel(WorkModel.HYBRID);
        } else if (inPersonRadio.isSelected()) {
            filters.setWorkModel(WorkModel.IN_PERSON);
        } else {
            filters.setWorkModel(WorkModel.NO_PREFERENCE);
        }
        
        // Location
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        if (!city.isEmpty()) filters.setCity(city);
        if (!state.isEmpty()) filters.setState(state);
        
        // Experience Level
        if (juniorRadio.isSelected()) {
            filters.setExperienceLevel(ExperienceLevel.JUNIOR);
        } else if (midLevelRadio.isSelected()) {
            filters.setExperienceLevel(ExperienceLevel.MID_LEVEL);
        } else if (seniorRadio.isSelected()) {
            filters.setExperienceLevel(ExperienceLevel.SENIOR);
        } else {
            filters.setExperienceLevel(ExperienceLevel.NO_PREFERENCE);
        }
        
        return filters;
    }
    
    private void searchJobs() {
        SearchFilters filters = getFiltersFromUI();
        
        if (filters.getSearchTerms().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter search terms");
            return;
        }
        
        searchButton.setEnabled(false);
        exportButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Searching job boards...");
        
        Timer statusTimer = new Timer(500, e -> {
            if (progressBar.isVisible()) {
                String currentText = statusLabel.getText();
                if (!currentText.endsWith("...")) {
                    statusLabel.setText(currentText + ".");
                } else {
                    statusLabel.setText("Searching job boards");
                }
            }
        });
        statusTimer.start();
        
        CompletableFuture.supplyAsync(() -> {
            WebScraper scraper = new WebScraper();
            return scraper.searchJobs(filters);
        }).thenAccept(jobs -> {
            SwingUtilities.invokeLater(() -> {
                statusTimer.stop();
                currentJobs = jobs;
                tableModel.setJobs(jobs);
                searchButton.setEnabled(true);
                exportButton.setEnabled(!jobs.isEmpty());
                progressBar.setVisible(false);
                
                if (jobs.isEmpty()) {
                    statusLabel.setText("No jobs found");
                    JOptionPane.showMessageDialog(this, 
                        "No jobs were found. This might be due to:\n" +
                        "• Network connectivity issues\n" +
                        "• Job boards blocking automated access\n" +
                        "• No matches for your search criteria\n\n" +
                        "Try different search terms or filters, or check the log file for details.",
                        "No Results", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText(String.format("Found %d jobs from %d sources", 
                        jobs.size(), 
                        jobs.stream().map(JobPosting::getSource).distinct().count()));
                    
                    Map<String, Long> sourceCounts = jobs.stream()
                        .collect(Collectors.groupingBy(JobPosting::getSource, Collectors.counting()));
                    
                    StringBuilder sourceInfo = new StringBuilder("Jobs by source:\n");
                    sourceCounts.forEach((source, count) -> 
                        sourceInfo.append(String.format("• %s: %d\n", source, count)));
                    
                    JOptionPane.showMessageDialog(this, 
                        sourceInfo.toString(), 
                        "Search Complete", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                statusTimer.stop();
                searchButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText("Error occurred");
                
                JOptionPane.showMessageDialog(this, 
                    "An error occurred during the search:\n" + ex.getMessage() +
                    "\n\nCheck the log file for details.",
                    "Search Error", 
                    JOptionPane.ERROR_MESSAGE);
            });
            ex.printStackTrace();
            return null;
        });
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("job_search_results.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            
            try {
                ExcelExporter.export(currentJobs, file);
                statusLabel.setText("Exported to: " + file.getName());
                JOptionPane.showMessageDialog(this, "Export successful!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Export failed: " + e.getMessage());
            }
        }
    }
}