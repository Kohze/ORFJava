/**
*@author Robin Kohze
*@version 0.1 15-04-2018
*Email  : Robin@Kohze.com
*Input  : On start it expects choosing a fasta file. 
**/

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

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
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


public class ORFAnalysis {
  public static void main(String[] args) {
    SwingContainer mainDisplay = new SwingContainer();
    mainDisplay.startDisplay();
  }
}

/**
*class SwingContainer
*Description: The GUI output that displays all fasta stats. 
*Returns    : Graphical output and asks for a fasta file as input. 
*Properties : none.
*Methods    : startDisplay();
**/
class SwingContainer extends JFrame implements ActionListener {
  FlowLayout experimentLayout = new FlowLayout();
  JTextArea textAreaAllSeq    = new JTextArea("", 42, 95);
  JTextArea textAreaSummary   = new JTextArea("", 20, 41); 
  JTextArea textArea   		  = new JTextArea("", 16, 34);
  JButton helpButton          = new JButton("help");
  JButton ntSequenceButton    = new JButton("Open Reading Frames");
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
        reader.ArrayList();

        exportButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(".");
            int userSelection = fileChooser.showSaveDialog(frame);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
  				File file = fileChooser.getSelectedFile();
  				reader.exportCollection(file.getAbsolutePath());
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

        ntSequenceButton.addActionListener(new ActionListener() {
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
  	  	  	reader.getStats();
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

        XYSeriesCollection scatterDataset  = new XYSeriesCollection();
        XYSeries seriesORF = new XYSeries("Open Reading Frames");
      	DefaultPieDataset PieDataset       = new DefaultPieDataset();
	    String allSequencesVar             = reader.getNTBasePairs();

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

  		PieDataset sourcePieDataset = PieDataset;

  		JFreeChart chart = ChartFactory.createScatterPlot("Open Reading Frame Overview", 
        												  "GC content [in %]", "ORF Length [in bp]", scatterDataset, 
        												  PlotOrientation.VERTICAL, 
        												  true, true, false);

        JFreeChart chart2 = ChartFactory.createPieChart("Nucleotide Distribution", 
        												sourcePieDataset, 
        												true, true, false);

        PiePlot newPieChart    = (PiePlot) chart2.getPlot();
        XYPlot newScatterChart = (XYPlot)  chart.getPlot();
        newPieChart.setCircular(true);
        newPieChart.setLabelGap(0.02);
        newPieChart.setSimpleLabels(true);
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator("{0}: {1}");
        newPieChart.setLabelGenerator(gen);
        ChartPanel pieChart = new ChartPanel(chart2);
        ChartPanel barChart = new ChartPanel(chart);

    	//Generating the UI with individual
        frame.setLayout(experimentLayout);
        frame.add(ntSequenceButton);
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
	//functions to control elements shown in view
	private void showtextAreaAllSeq(boolean show){
		    scroll.setVisible(show);
		    logoLabel.setVisible(false);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

	private void showtextAreaSummary(boolean show){
		    textAreaSummary.setVisible(show);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

	private void showPieChart(boolean show){
			pieChartPanel.setVisible(show);
			logoLabel.setVisible(false);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

	private void showBarChart(boolean show){
			barChartPanel.setVisible(show);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

}

/**
*Sequence Class
*Description: General class to fasta sequence properties. 
*Properties : header, sequence.
*Methods    : getSeq(); getHeader();
**/
class OpenReadingFrame implements Comparable<OpenReadingFrame> {

  String ntSequence;
  Integer bpLength, bpStart, bpEnd;

  OpenReadingFrame(String ntSequence, Integer bpStart, Integer bpEnd) {
    this.ntSequence = ntSequence;
    this.bpStart    = bpStart;
    this.bpEnd      = bpEnd; 
    this.bpLength   = this.ntSequence.length();
  }

  //methods to export properties  
  public String getSequence(){
    return this.ntSequence;
  }

  public Integer getBpStart(){
  	return this.bpStart;
  }

  public Integer getBpEnd(){
  	return this.bpEnd;
  }

  public Integer getBpLength(){
  	return this.bpLength;
  }

  //round function to not have 10 decimals in the percentage view.
  public double round(double value, int decimals) {
    double factor = (double) Math.pow(10, decimals);
    value *= factor;
    double tmp = Math.round(value);
    return (double) tmp / factor;
  }

  //calculates the GC/AT percentage
  public double getGCPercentage(){
  	double gcCounter = 0;
  	for(char i : ntSequence.toCharArray()){
  		if(i == 'G' | i == 'C'){
  			gcCounter += 1;
  		}
  	}
  	return round(gcCounter*100/ (double) bpLength, 1);
  }

  //comparable method to sort the sequences by length
  public int compareTo(OpenReadingFrame other){
    return this.bpLength.compareTo(other.bpLength);
  }

  @Override
  public String toString() {
     return new StringBuffer(this.ntSequence)
    .append("\n> Length: ")
    .append(this.bpLength + "bp  |  GC content: " + getGCPercentage() +"%")
    .append("  |  Start Position: " + getBpStart() +  "bp  |  End Position: " + getBpEnd() + "bp\n\n").toString();
  }
} 

/**
*Sequence FastaReader
*Description: Reads fasta sequence with path input and assigns it to Array 
*Returns    : Prints all sequences alphabetically sorted. 
*Properties : header, sequence.
*Methods    : getSeq(); getHeader();
**/
class ORFCollector {
    String path, header, aaSeq, ntSequenceString, line, regexPattern;
    int    lengthSum, minORFLength, maxORFLength;
    ArrayList<OpenReadingFrame> ORFCollection = new ArrayList<OpenReadingFrame>();

    ORFCollector(String path, Integer minORFLength, Integer maxORFLength) {
        this.path = path;
        this.regexPattern = "ATG(?:[ATGC]{3}){" + minORFLength +","+ maxORFLength + "}?(?:TAA|TAG|TGA)";
    }
    
     void ArrayList(){
      try{
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        
        ArrayList<OpenReadingFrame> ORFCollection = new ArrayList<OpenReadingFrame>();
        StringBuilder aaSeq = new StringBuilder();
        while ((line = reader.readLine()) != null) aaSeq.append(line); 
        ntSequenceString = aaSeq.toString();
        this.lengthSum += ntSequenceString.length();

        //regExpression for the ORF region matching.
        Pattern checkRegex = Pattern.compile(regexPattern);
		Matcher regexMatcher = checkRegex.matcher(ntSequenceString);
		while (regexMatcher.find()){
			if (regexMatcher.group().length() != 0){
				OpenReadingFrame ORFobject  = new OpenReadingFrame(regexMatcher.group(), regexMatcher.start(), regexMatcher.end());
				ORFCollection.add(ORFobject);
			}
		}
        this.ORFCollection = ORFCollection;  
      } catch (FileNotFoundException e){
          e.printStackTrace();
      } catch (IOException e){
          e.printStackTrace();
      }
    }

    public void printSorted(){
      Collections.sort(ORFCollection);
      System.out.print(ORFCollection);
    }

    public ArrayList<OpenReadingFrame> returnORFCollection(){
    	return this.ORFCollection;
    }

    public void exportCollection(String path){
    	try{
    		PrintWriter csvWriter = new PrintWriter(new File(path));
    		StringBuilder sb = new StringBuilder();
    		Collections.sort(this.ORFCollection);
    		//header column
    		sb.append("sequence,length,start,end,GCcontent\n");
  			
  			//data columns
    		for(OpenReadingFrame o : this.ORFCollection){
    			sb.append(o.getSequence());
    			sb.append(',');
    			sb.append(o.getBpLength());
    			sb.append(',');
    			sb.append(o.getBpStart());
    			sb.append(',');
    			sb.append(o.getBpEnd());
    			sb.append(',');
    			sb.append(o.getGCPercentage());
    			sb.append('\n');
    		}

    		csvWriter.write(sb.toString());
    		csvWriter.close();
   	  } catch (FileNotFoundException e){
          e.printStackTrace();
      } 
    }

    public String getNTBasePairs(){
      return ntSequenceString;
    }

    void getStats(){
      System.out.println("Open Reading Frame Summary");
      System.out.print("Total Number of Open Reading Frames: ");
      System.out.println(ORFCollection.size());
      System.out.println();
      System.out.println("Longest Open Reading Frame: ");
      System.out.println(Collections.max(ORFCollection));
      System.out.println();
      System.out.println("Shortest Open Reading Frame: ");
      System.out.println(Collections.min(ORFCollection));
      System.out.println();
      System.out.println("Average Open Reading Frame Length: ");
      System.out.println(lengthSum/ORFCollection.size() + "bp");
    }
}