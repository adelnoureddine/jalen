/*******************************************************************************
 * Copyright (c) 2012 Adel Noureddine.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Adel Noureddine - initial API and implementation
 ******************************************************************************/
package jalen.console.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class PieChart extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private String title;
	
	private List<Object[][]> list = new ArrayList<Object[][]>();
	private PiePlot plot;

	public PieChart(int pid, String title) {
		this.title = title;
		this.setTitle("Jalen Client: " + title);
		this.setSize(1000, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel label = new JLabel("Java Process ID: " + pid);
		this.add(label, BorderLayout.PAGE_START);
		
		initialize();
	}
	
	private void initialize() {
		JFreeChart chart = createChart(new DefaultPieDataset());
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
		this.add(chartPanel, BorderLayout.CENTER);
		
		this.pack();
		this.setVisible(true);
	}
	
	public void addData(Object[][] data) {
		this.list.add(data);
	}
	
	public void updateDataset() {
		DefaultPieDataset dataset = new DefaultPieDataset();
		
		for (Object[][] data : this.list) {
			dataset.setValue((Comparable<?>) data[0][0], (Number) data[0][1]);
		}
		
		this.plot.setDataset(dataset);
	}
	
	private JFreeChart createChart(PieDataset dataset) {
		// create a chart...
		JFreeChart chart = ChartFactory.createPieChart(
				"Jalen Client: " + this.title,
				dataset,
				false, // legend?
				true, // tooltips?
				false // URLs?
				);
		
		this.plot = (PiePlot) chart.getPlot();
		this.plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} ({2})"));
		this.plot.setNoDataMessage("No data available");
		this.plot.setCircular(true);
		return chart;
	}
}
