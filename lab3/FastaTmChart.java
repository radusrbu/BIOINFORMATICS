import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class FastaTmChart {
    public static void main(String[] args) {
		final String fastaPath = "C:\\Users\\rsp\\Desktop\\Desktop\\faculty\\an 4 sm 1\\sample_dna.fasta"; // set your FASTA path
		final int windowSize = 9; // sliding window size
		final double sodiumMilliMolar = 50.0; // Na+ concentration in mM
		final double threshold = .0; // user-settable threshold (°C)

        String sequence;
        try {
            sequence = readFastaSequence(fastaPath);
        } catch (IOException e) {
            System.err.println("Failed to read FASTA: " + e.getMessage());
            return;
        }
        if (sequence.isEmpty()) {
            System.err.println("No sequence found in FASTA.");
            return;
        }

        sequence = sequence.toUpperCase();
        if (sequence.length() < windowSize) {
            System.err.println("Sequence shorter than window size (" + windowSize + ").");
            return;
        }

        double[] wallace = new double[sequence.length() - windowSize + 1];
        double[] saltAdj = new double[sequence.length() - windowSize + 1];

        for (int i = 0; i <= sequence.length() - windowSize; i++) {
            String kmer = sequence.substring(i, i + windowSize);
            if (!isValidDNA(kmer)) {
                System.err.println("Encountered non-ACGT character in window at position " + (i + 1) + ". Skipping.");
            }
            wallace[i] = computeWallaceTm(kmer);
            saltAdj[i] = computeSaltAdjustedTm(kmer, sodiumMilliMolar);
        }

		// compute global min/max across both signals
		double globalMin = Double.POSITIVE_INFINITY;
		double globalMax = Double.NEGATIVE_INFINITY;
		for (double v : wallace) { if (v < globalMin) globalMin = v; if (v > globalMax) globalMax = v; }
		for (double v : saltAdj) { if (v < globalMin) globalMin = v; if (v > globalMax) globalMax = v; }
		final double gMin = globalMin;
		final double gMax = globalMax;

		final String title = "Sliding Window Tm (window=" + windowSize + ", thr=" + threshold + ")";
        SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame(title);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(new Dimension(1000, 700));
			frame.setLocationByPlatform(true);

			javax.swing.JPanel container = new javax.swing.JPanel(new java.awt.BorderLayout());
			container.setBackground(java.awt.Color.white);
			LineChartPanel topChart = new LineChartPanel(wallace, saltAdj, windowSize, gMin, gMax, threshold);
			BarsPanel barsPanel = new BarsPanel(wallace, saltAdj, threshold);
			container.add(topChart, java.awt.BorderLayout.CENTER);
			container.add(barsPanel, java.awt.BorderLayout.SOUTH);
			frame.setContentPane(container);
			frame.pack();
			frame.setVisible(true);
        });
    }

    private static String readFastaSequence(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith(">")) continue; // header
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static boolean isValidDNA(String dna) {
        for (int i = 0; i < dna.length(); i++) {
            char c = dna.charAt(i);
            if (c != 'A' && c != 'C' && c != 'G' && c != 'T' && c != 'a' && c != 'c' && c != 'g' && c != 't') {
                return false;
            }
        }
        return true;
    }

    private static int countChar(String s, char target) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == target) count++;
        }
        return count;
    }

    private static int computeWallaceTm(String dna) {
        String u = dna.toUpperCase();
        int a = countChar(u, 'A');
        int c = countChar(u, 'C');
        int g = countChar(u, 'G');
        int t = countChar(u, 'T');
        return 4 * (g + c) + 2 * (a + t);
    }

    private static double computeSaltAdjustedTm(String dna, double sodiumMilliMolar) {
        String u = dna.toUpperCase();
        int length = u.length();
        int c = countChar(u, 'C');
        int g = countChar(u, 'G');
        double gcPercent = ((g + c) * 100.0) / Math.max(1, length);
        double sodiumMolar = sodiumMilliMolar / 1000.0; // mM -> M
        return 81.5 + 16.6 * log10(sodiumMolar) + 0.41 * gcPercent - (600.0 / length);
    }

    private static double log10(double x) {
        return Math.log(x) / Math.log(10.0);
    }

	private static class LineChartPanel extends JPanel {
		private final double[] series1; // Wallace
		private final double[] series2; // Salt-adjusted
		private final int windowSize;
		private final double globalMin;
		private final double globalMax;
		private final double threshold;

		LineChartPanel(double[] series1, double[] series2, int windowSize, double globalMin, double globalMax, double threshold) {
			this.series1 = series1;
			this.series2 = series2;
			this.windowSize = windowSize;
			this.globalMin = globalMin;
			this.globalMax = globalMax;
			this.threshold = threshold;
			setBackground(Color.white);
		}

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(900, 480);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int left = 70;
            int right = 20;
            int top = 40;
            int bottom = 60;

            int plotW = Math.max(1, w - left - right);
            int plotH = Math.max(1, h - top - bottom);

            // axes
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(left, h - bottom, left + plotW, h - bottom); // X
            g2.drawLine(left, h - bottom, left, top); // Y

            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for (double v : series1) { minY = Math.min(minY, v); maxY = Math.max(maxY, v); }
            for (double v : series2) { minY = Math.min(minY, v); maxY = Math.max(maxY, v); }
            if (Double.isInfinite(minY) || Double.isInfinite(maxY)) return;
            if (minY == maxY) { maxY = minY + 1.0; }

            // paddings for aesthetics
            double yPad = (maxY - minY) * 0.1;
            minY -= yPad;
            maxY += yPad;

            int n = series1.length;
            // map i -> x
            double xStep = n > 1 ? (double) plotW / (n - 1) : plotW;

            // grid & ticks
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            g2.setColor(new Color(230, 230, 230));
            int yTicks = 6;
            for (int i = 0; i <= yTicks; i++) {
                int y = (int) (h - bottom - (i * (double) plotH / yTicks));
                g2.drawLine(left, y, left + plotW, y);
            }
            g2.setColor(Color.DARK_GRAY);
            DecimalFormat df = new DecimalFormat("0.0");
            for (int i = 0; i <= yTicks; i++) {
                double value = minY + (i * (maxY - minY) / yTicks);
                int y = (int) (h - bottom - (i * (double) plotH / yTicks));
                String label = df.format(value);
                int strW = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, left - 10 - strW, y + 4);
            }

            // series 1: Wallace (red)
            Path2D path1 = new Path2D.Double();
            for (int i = 0; i < n; i++) {
                double x = left + i * xStep;
                double y = h - bottom - ((series1[i] - minY) / (maxY - minY)) * plotH;
                if (i == 0) path1.moveTo(x, y); else path1.lineTo(x, y);
            }
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(220, 20, 60));
            g2.draw(path1);

            // series 2: Salt-adjusted (blue)
            Path2D path2 = new Path2D.Double();
            for (int i = 0; i < n; i++) {
                double x = left + i * xStep;
                double y = h - bottom - ((series2[i] - minY) / (maxY - minY)) * plotH;
                if (i == 0) path2.moveTo(x, y); else path2.lineTo(x, y);
            }
            g2.setColor(new Color(30, 144, 255));
            g2.draw(path2);

            // legend
            int legendX = left + 10;
            int legendY = top + 10;
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            g2.setColor(new Color(220, 20, 60));
            g2.fillRect(legendX, legendY, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Wallace", legendX + 18, legendY + 11);
            g2.setColor(new Color(30, 144, 255));
            g2.fillRect(legendX + 90, legendY, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Salt-adjusted", legendX + 108, legendY + 11);

			// titles
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            String title = "Tm along sequence (window=" + windowSize + ")";
            int titleW = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, left + (plotW - titleW) / 2, top - 10);

			// min/max/threshold info
			g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
			String stats = "Min: " + df.format(globalMin) + "  Max: " + df.format(globalMax) + "  Thr: " + df.format(threshold);
			g2.drawString(stats, left, top - 28);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
            String xLabel = "Window start index";
            int xLabelW = g2.getFontMetrics().stringWidth(xLabel);
            g2.drawString(xLabel, left + (plotW - xLabelW) / 2, h - 20);

            // Y label rotated
            String yLabel = "Tm (°C)";
            Graphics2D g2r = (Graphics2D) g2.create();
            g2r.rotate(-Math.PI / 2);
            int yLabelW = g2r.getFontMetrics().stringWidth(yLabel);
            g2r.drawString(yLabel, -(top + (plotH + yLabelW) / 2), 20);
            g2r.dispose();
        }
    }

	private static class BarsPanel extends JPanel {
		private final double[] series1; // Wallace
		private final double[] series2; // Salt-adjusted
		private final double threshold;

		BarsPanel(double[] series1, double[] series2, double threshold) {
			this.series1 = series1;
			this.series2 = series2;
			this.threshold = threshold;
			setBackground(Color.white);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(900, 200);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();
			int left = 70;
			int right = 20;
			int top = 20;
			int bottom = 40;
			int plotW = Math.max(1, w - left - right);
			int plotH = Math.max(1, h - top - bottom);

			int rows = 2;
			int rowGap = 10;
			int barHeight = (plotH - rowGap) / rows;

			int n = Math.max(series1.length, series2.length);
			double xStep = n > 0 ? (double) plotW / n : plotW;

			// labels
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
			g2.setColor(Color.DARK_GRAY);
			g2.drawString("Above threshold (" + new DecimalFormat("0.0").format(threshold) + " °C)", left, top - 4);

			// Wallace row
			int y1 = top;
			g2.drawString("Wallace", 10, y1 + barHeight - 2);
			for (int i = 0; i < series1.length; i++) {
				double v = series1[i];
				if (v >= threshold) {
					int x = (int) (left + i * xStep);
					int width = (int) Math.ceil(xStep);
					g2.setColor(new Color(220, 20, 60));
					g2.fillRect(x, y1, width, barHeight);
				}
			}

			// Salt-adjusted row
			int y2 = top + barHeight + rowGap;
			g2.setColor(Color.DARK_GRAY);
			g2.drawString("Salt-adjusted", 10, y2 + barHeight - 2);
			for (int i = 0; i < series2.length; i++) {
				double v = series2[i];
				if (v >= threshold) {
					int x = (int) (left + i * xStep);
					int width = (int) Math.ceil(xStep);
					g2.setColor(new Color(30, 144, 255));
					g2.fillRect(x, y2, width, barHeight);
				}
			}

			// X-axis index ticks
			g2.setColor(new Color(230, 230, 230));
			for (int i = 0; i <= n; i += Math.max(1, n / 10)) {
				int x = (int) (left + i * xStep);
				g2.drawLine(x, top, x, top + barHeight * 2 + rowGap);
			}
			g2.setColor(Color.DARK_GRAY);
			g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
			for (int i = 0; i <= n; i += Math.max(1, n / 10)) {
				int x = (int) (left + i * xStep);
				String label = String.valueOf(i);
				int sw = g2.getFontMetrics().stringWidth(label);
				g2.drawString(label, x - sw / 2, h - 18);
			}
			g2.drawString("Window start index", left + (plotW - g2.getFontMetrics().stringWidth("Window start index")) / 2, h - 4);
		}
	}
}


