import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;

public class DNASequenceAnalyzer extends JFrame {
    private JTextArea resultArea;
    private JButton selectFileButton;
    private JLabel statusLabel;
    private ChartPanel chartPanel;
    private double[] aFrequencies, tFrequencies, cFrequencies, gFrequencies;
    
    public DNASequenceAnalyzer() {
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("DNA Sequence Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create components
        selectFileButton = new JButton("Select FASTA File");
        resultArea = new JTextArea(10, 60);
        statusLabel = new JLabel("Select a FASTA file to analyze");
        chartPanel = new ChartPanel();
        
        // Configure components
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(Color.BLACK);
        resultArea.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Create split pane for text and chart
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, chartPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.4);
        
        // Add components to frame
        add(selectFileButton, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // Add event listener
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAndAnalyzeFile();
            }
        });
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void selectAndAnalyzeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("FASTA Files", "fasta", "fa", "fas"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            statusLabel.setText("Analyzing file: " + selectedFile.getName());
            
            try {
                String sequence = readFASTAFile(selectedFile);
                if (sequence != null && !sequence.isEmpty()) {
                    analyzeSequence(sequence);
                    statusLabel.setText("Analysis complete!");
                } else {
                    JOptionPane.showMessageDialog(this, "No valid DNA sequence found in the file.", "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error: No valid sequence found");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error reading file");
            }
        }
    }
    
    private String readFASTAFile(File file) throws IOException {
        StringBuilder sequence = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inSequence = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith(">")) {
                    // Header line - start of new sequence
                    inSequence = true;
                } else if (inSequence && !line.isEmpty()) {
                    // Sequence line - remove any whitespace and convert to uppercase
                    sequence.append(line.replaceAll("\\s+", "").toUpperCase());
                }
            }
        }
        
        // Filter out non-DNA characters (keep only A, T, C, G)
        String filteredSequence = sequence.toString().replaceAll("[^ATCG]", "");
        
        return filteredSequence;
    }
    
    private void analyzeSequence(String sequence) {
        if (sequence.length() < 30) {
            JOptionPane.showMessageDialog(this, "Sequence too short (minimum 30 nucleotides required)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        resultArea.setText("");
        resultArea.append("DNA Sequence Analysis - Sliding Window (30 nucleotides)\n");
        resultArea.append("Sequence length: " + sequence.length() + " nucleotides\n");
        resultArea.append("Number of windows: " + (sequence.length() - 29) + "\n\n");
        
        // Initialize arrays to store cumulative frequencies
        aFrequencies = new double[sequence.length() - 29];
        tFrequencies = new double[sequence.length() - 29];
        cFrequencies = new double[sequence.length() - 29];
        gFrequencies = new double[sequence.length() - 29];
        
        // Calculate frequencies for each window
        for (int i = 0; i <= sequence.length() - 30; i++) {
            String window = sequence.substring(i, i + 30);
            
            // Count nucleotides in current window
            int aCount = 0, tCount = 0, cCount = 0, gCount = 0;
            for (char nucleotide : window.toCharArray()) {
                switch (nucleotide) {
                    case 'A': aCount++; break;
                    case 'T': tCount++; break;
                    case 'C': cCount++; break;
                    case 'G': gCount++; break;
                }
            }
            
            // Calculate relative frequencies (percentages)
            aFrequencies[i] = (double) aCount / 30 * 100;
            tFrequencies[i] = (double) tCount / 30 * 100;
            cFrequencies[i] = (double) cCount / 30 * 100;
            gFrequencies[i] = (double) gCount / 30 * 100;
        }
        
        // Display results as 4 lines
        resultArea.append("A: ");
        for (int i = 0; i < aFrequencies.length; i++) {
            resultArea.append(String.format("%.1f", aFrequencies[i]));
            if (i < aFrequencies.length - 1) resultArea.append(" ");
        }
        resultArea.append("\n");
        
        resultArea.append("T: ");
        for (int i = 0; i < tFrequencies.length; i++) {
            resultArea.append(String.format("%.1f", tFrequencies[i]));
            if (i < tFrequencies.length - 1) resultArea.append(" ");
        }
        resultArea.append("\n");
        
        resultArea.append("C: ");
        for (int i = 0; i < cFrequencies.length; i++) {
            resultArea.append(String.format("%.1f", cFrequencies[i]));
            if (i < cFrequencies.length - 1) resultArea.append(" ");
        }
        resultArea.append("\n");
        
        resultArea.append("G: ");
        for (int i = 0; i < gFrequencies.length; i++) {
            resultArea.append(String.format("%.1f", gFrequencies[i]));
            if (i < gFrequencies.length - 1) resultArea.append(" ");
        }
        resultArea.append("\n");
        
        // Add summary statistics
        resultArea.append("\n=== SUMMARY STATISTICS ===\n");
        resultArea.append("Overall composition:\n");
        
        int totalA = 0, totalT = 0, totalC = 0, totalG = 0;
        for (char nucleotide : sequence.toCharArray()) {
            switch (nucleotide) {
                case 'A': totalA++; break;
                case 'T': totalT++; break;
                case 'C': totalC++; break;
                case 'G': totalG++; break;
            }
        }
        
        double totalLength = sequence.length();
        resultArea.append(String.format("A: %.2f%% (%d/%d)\n", (totalA/totalLength)*100, totalA, (int)totalLength));
        resultArea.append(String.format("T: %.2f%% (%d/%d)\n", (totalT/totalLength)*100, totalT, (int)totalLength));
        resultArea.append(String.format("C: %.2f%% (%d/%d)\n", (totalC/totalLength)*100, totalC, (int)totalLength));
        resultArea.append(String.format("G: %.2f%% (%d/%d)\n", (totalG/totalLength)*100, totalG, (int)totalLength));
        
        // Update the chart with the frequency data
        chartPanel.updateChart(aFrequencies, tFrequencies, cFrequencies, gFrequencies);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DNASequenceAnalyzer().setVisible(true);
            }
        });
    }
}

class ChartPanel extends JPanel {
    private double[] aFreq, tFreq, cFreq, gFreq;
    private boolean hasData = false;
    
    public ChartPanel() {
        setPreferredSize(new Dimension(800, 400));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Nucleotide Frequency Chart"));
    }
    
    public void updateChart(double[] aFrequencies, double[] tFrequencies, double[] cFrequencies, double[] gFrequencies) {
        this.aFreq = aFrequencies.clone();
        this.tFreq = tFrequencies.clone();
        this.cFreq = cFrequencies.clone();
        this.gFreq = gFrequencies.clone();
        this.hasData = true;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!hasData) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Select a FASTA file to see the chart", 250, 200);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth() - 100;
        int height = getHeight() - 100;
        int startX = 50;
        int startY = 50;
        
        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(startX, startY, startX, startY + height);
        g2d.drawLine(startX, startY + height, startX + width, startY + height);
        
        // Draw grid lines
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 10; i++) {
            int y = startY + (i * height / 10);
            g2d.drawLine(startX, y, startX + width, y);
            g2d.drawString(String.valueOf(100 - i * 10), startX - 25, y + 5);
        }
        
        // Draw labels
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Position", startX + width / 2, startY + height + 25);
        
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Frequency (%)", -startY - height / 2, 25);
        g2d.rotate(Math.PI / 2);
        
        // Draw nucleotide frequency lines
        if (aFreq != null && aFreq.length > 0) {
            drawLine(g2d, aFreq, Color.RED, "A", startX, startY, width, height);
            drawLine(g2d, tFreq, Color.BLUE, "T", startX, startY, width, height);
            drawLine(g2d, cFreq, Color.GREEN, "C", startX, startY, width, height);
            drawLine(g2d, gFreq, Color.ORANGE, "G", startX, startY, width, height);
        }
        
        // Draw legend
        drawLegend(g2d, startX + width - 100, startY + 20);
    }
    
    private void drawLine(Graphics2D g2d, double[] frequencies, Color color, String label, 
                         int startX, int startY, int width, int height) {
        if (frequencies == null || frequencies.length == 0) return;
        
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < frequencies.length - 1; i++) {
            int x1 = startX + (i * width / (frequencies.length - 1));
            int y1 = startY + height - (int)(frequencies[i] * height / 100);
            int x2 = startX + ((i + 1) * width / (frequencies.length - 1));
            int y2 = startY + height - (int)(frequencies[i + 1] * height / 100);
            
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }
    
    private void drawLegend(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        String[] labels = {"A", "T", "C", "G"};
        
        for (int i = 0; i < labels.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fillRect(x, y + i * 20, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawString(labels[i], x + 20, y + i * 20 + 12);
        }
    }
}

