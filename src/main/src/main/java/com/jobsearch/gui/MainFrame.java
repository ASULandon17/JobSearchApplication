package com.jobsearch.gui;

import com.jobsearch.model.JobPosting;
import com.jobsearch.scraper.WebScraper;
import com.jobsearch.utils.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    private File selectedResume;
    
    public MainFrame() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Job Search Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Resume:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        resumeLabel = new JLabel("No file selected");
        resumeLabel.setForeground(Color.GRAY);
        panel.add(resumeLabel, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        selectResumeButton = new JButton("Select Resume");
        selectResumeButton.addActionListener(e -> selectResume());
        panel.add(selectResumeButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Search Terms:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        searchField = new JTextField();
        searchField.setToolTipText("Enter job search terms (e.g., 'junior software engineer')");
        panel.add(searchField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        searchButton = new JButton("Search Jobs");
        searchButton.addActionListener(e -> searchJobs());
        searchButton.setEnabled(false);
        panel.add(searchButton, gbc);
        
        gbc.gridx = 3;
        exportButton = new JButton("Export to Excel");
        exportButton.addActionListener(e -> exportToExcel());
        exportButton.setEnabled(false);
        panel.add(exportButton, gbc);
        
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
        
        TableRowSorter<JobTableModel> sorter = new TableRowSorter<>(tableModel);
        jobTable.setRowSorter(sorter);
        
        return new JScrollPane(jobTable);
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Ready");
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
    
    private void searchJobs() {
        String searchTerms = searchField.getText().trim();
        if (searchTerms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter search terms");
            return;
        }
        
        searchButton.setEnabled(false);
        exportButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Searching for jobs...");
        
        CompletableFuture.supplyAsync(() -> {
            WebScraper scraper = new WebScraper();
            return scraper.searchJobs(searchTerms);
        }).thenAccept(jobs -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.setJobs(jobs);
                searchButton.setEnabled(true);
                exportButton.setEnabled(!jobs.isEmpty());
                progressBar.setVisible(false);
                statusLabel.setText("Found " + jobs.size() + " jobs");
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Error searching jobs: " + ex.getMessage());
                searchButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText("Error occurred during search");
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
                ExcelExporter.export(tableModel.getJobs(), file);
                statusLabel.setText("Exported to: " + file.getName());
                JOptionPane.showMessageDialog(this, "Export successful!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Export failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}