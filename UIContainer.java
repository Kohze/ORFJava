import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.awt.*;
import java.lang.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

/**
*class UIContainer
*@author Robin Kohze
*@Version 0.11 15-04-2018
*Description: The Graphical User Interface of the ORFAnalysis Tool. 
*Returns    : Graphical output and asks for a .txt file as input. 
*Properties : none.
*Methods    : startDisplay(); showtextAreaAllSeq(); showtextAreaSummary(); showPieChart(); showBarChart();
*/
public class UIContainer extends JFrame implements ActionListener {
  FlowLayout experimentLayout = new FlowLayout();
  JTextArea textAreaAllSeq    = new JTextArea("", 42, 95);
  JTextArea textAreaSummary   = new JTextArea("", 20, 41); 
  JTextArea textArea   		  = new JTextArea("", 16, 34);
  JButton helpButton          = new JButton("help");
  JButton fullSequenceTab     = new JButton("Open Reading Frames");
  JButton summButton          = new JButton("Summary");
  JButton compButton          = new JButton("Composition");
  JButton exportButton        = new JButton("Export");
  JFrame  frame               = new JFrame();
  JPanel  pieChartPanel       = new JPanel();
  JPanel  barChartPanel       = new JPanel();
  JScrollPane scroll          = new JScrollPane(textAreaAllSeq);
  ImageIcon pic               = new ImageIcon("ORFAnalysis.jpg");
  JLabel logoLabel            = new JLabel(pic);

  private JFileChooser fileChooser;
  public void actionPerformed(ActionEvent e) {}

  void startDisplay(){
      File selectedFile;
	  int reply;
      fileChooser = new JFileChooser(".");
      reply = fileChooser.showSaveDialog(null);
      if (reply == JFileChooser.APPROVE_OPTION) {
        selectedFile = fileChooser.getSelectedFile();
        ORFCollector reader = new ORFCollector(selectedFile.getAbsolutePath(), 10, 100);
        reader.getORFArray();

        //The action listeners of all buttons
        exportButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(".");
            int userSelection = fileChooser.showSaveDialog(frame);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
  				File file = fileChooser.getSelectedFile();
  				reader.exportORFCollection(file.getAbsolutePath());
			}
          }
        });
                 
        helpButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
          	JFrame helpFrame = new JFrame("help and reference");
          	JPanel helpPanel = new JPanel();
          	helpPanel.add(textArea);
          	helpFrame.add(helpPanel);

          	helpFrame.setVisible(true);
          	helpFrame.setSize(400, 305);

            textArea.setText("ORFAnalyser Manual: \n"
            				 + "To start the program, please load a nucleotide containing .txt file." 
            				 + "\n\nThe ORFAnalyser allows a researcher to inspect the various OpenReadingFrames of a given sequence" 
            				 + "\n\nThe main functionality includes the overview of all Open Reading frames, the Summary and Comparison tab." 
            				 + "\n\nTo Export given Open Reading Frames, please click the Export Button and choose your storage destination. Please attach a .csv at the end of the name"
            				 + "\n\nFind a full overview and source code at https://github.com/Kohze/ORFJava");
          }
        });

        compButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            showtextAreaSummary(false);
            showtextAreaAllSeq(false);
            showPieChart(true);
            showBarChart(false);
           }
        });

        fullSequenceTab.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
  	  	  	PrintStream old_out = System.out;
  	  	  	System.setOut(new PrintStream(pipeOut));
  		  	reader.printSorted();
  		  	System.setOut(old_out);
  	  	  	String output = new String(pipeOut.toByteArray());

  	  	  	//adding and formatting dataset to textarea
            textAreaAllSeq.setLineWrap(true);
            String formattedString = output.replaceAll("(.{120})", "$1\n").replaceAll(",\\s", "");
            textAreaAllSeq.append(formattedString);
            textAreaAllSeq.setCaretPosition(0);

            showtextAreaSummary(false);
            showtextAreaAllSeq(true);
            showPieChart(false);
            showBarChart(false);
          }
        });

        summButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) { 
            ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
  	  	  	PrintStream old_out = System.out;
  	  	  	System.setOut(new PrintStream(pipeOut));
  	  	  	reader.getORFStatistics();
  	  	  	System.setOut(old_out);
  	  	  	String output = new String(pipeOut.toByteArray());
            textAreaSummary.setText("");
            String formattedString = output.replaceAll("(.{50})", "$1\n");
            textAreaSummary.append(formattedString);

            showtextAreaSummary(true);
            showtextAreaAllSeq(false);
            showPieChart(false);
            showBarChart(true);
          }
        });

        //initializing Charts
        XYSeriesCollection scatterDataset = new XYSeriesCollection();
        XYSeries seriesORF                = new XYSeries("Open Reading Frames");
      	DefaultPieDataset PieDataset      = new DefaultPieDataset();
      	PieDataset sourcePieDataset       = PieDataset;
	    String allSequencesVar            = reader.returnBasePairs();

	    //Recalling Chart data from OpenReadingFrame Object Collections and transforming it to chart relevant formatting.
	    for(OpenReadingFrame entry : reader.returnORFCollection()) seriesORF.add(entry.getGCPercentage(), entry.getBpLength());
	    scatterDataset.addSeries(seriesORF);
        Map<Character,Integer> frequencies = new HashMap<>();
        for (char ch : allSequencesVar.toCharArray()) frequencies.put(ch, frequencies.getOrDefault(ch, 0) + 1);
  		for (Map.Entry entry : frequencies.entrySet()) {
  			ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
  	  	  	PrintStream old_out = System.out;
  	  	  	System.setOut(new PrintStream(pipeOut));
  	  	  	System.out.println(entry.getValue());
  	  	  	System.setOut(old_out);
  	  	  	String output = new String(pipeOut.toByteArray());
  			PieDataset.setValue("" + entry.getKey(), Double.parseDouble(output));
  		}


  		//Calling Chart creator functions "ChartFactories"
  		JFreeChart chart = ChartFactory.createScatterPlot("Open Reading Frame Overview", 
        												  "GC content [in %]", "ORF Length [in bp]", scatterDataset, 
        												  PlotOrientation.VERTICAL, 
        												  true, true, false);

        JFreeChart piePlot = ChartFactory.createPieChart("Nucleotide Distribution", 
        												sourcePieDataset, 
        												true, true, false);

        //integrating data into charts and setting label meta data
        PiePlot newPieChart    = (PiePlot) piePlot.getPlot();
        XYPlot newScatterChart = (XYPlot)  chart.getPlot();
        newPieChart.setCircular(true);
        newPieChart.setLabelGap(0.02);
        newPieChart.setSimpleLabels(true);
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator("{0}: {1}");
        newPieChart.setLabelGenerator(gen);
        ChartPanel pieChart = new ChartPanel(piePlot);
        ChartPanel barChart = new ChartPanel(chart);

    	//Generating the UI with individual
        frame.setLayout(experimentLayout);
        frame.add(fullSequenceTab);
        frame.add(summButton);
        frame.add(compButton);
        frame.add(helpButton);
        frame.add(exportButton);
        frame.add(barChartPanel);
        frame.add(pieChartPanel);
    	frame.add(scroll);
    	frame.add(textAreaSummary);
    	frame.add(logoLabel);
    	pieChartPanel.add(pieChart);
        barChartPanel.add(barChart);

        //set meta options
        frame.setTitle("Open Reading Frame Analyser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.setSize(1200, 800);
        frame.setVisible(true);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textAreaSummary.setVisible(false);
		scroll.setVisible(false);
		barChartPanel.setVisible(false);
		pieChartPanel.setVisible(false);
      }
}
	/** functions to control elements shown in view
	*	@param show A boolean to indicate whether to show or hide component
	*/
	private void showtextAreaAllSeq(boolean show){
		    scroll.setVisible(show);
		    logoLabel.setVisible(false);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}
	//@see showtextAreaAllSeq()
	private void showtextAreaSummary(boolean show){
		    textAreaSummary.setVisible(show);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

	//@see showtextAreaAllSeq
	private void showPieChart(boolean show){
			pieChartPanel.setVisible(show);
			logoLabel.setVisible(false);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

	//@see showtextAreaAllSeq
	private void showBarChart(boolean show){
			barChartPanel.setVisible(show);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}
}
