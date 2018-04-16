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
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

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
  JTextArea textArea          = new JTextArea("Welcome to the Open Reading Frame Analyzer", 25, 95);
  JButton helpButton          = new JButton("help");
  JButton ntSequenceButton    = new JButton("Open Reading Frames");
  JButton summButton          = new JButton("Summary");
  JButton compButton          = new JButton("Composition");
  JButton exportButton        = new JButton("Export");
  JFrame  frame               = new JFrame();
  JPanel  pieChartPanel       = new JPanel();
  JScrollPane scroll          = new JScrollPane (textArea);

  private JFileChooser fileChooser;
  public void actionPerformed(ActionEvent e) {}

  void startDisplay(){
      File selectedFile;
	  int reply;
      fileChooser = new JFileChooser(".");
      reply = fileChooser.showSaveDialog(null);
      if (reply == JFileChooser.APPROVE_OPTION) {
        selectedFile = fileChooser.getSelectedFile();
        ORFCollector reader = new ORFCollector(selectedFile.getAbsolutePath());
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
            textArea.setText("Please first load a .fasta file via the uploader.\nThe buttons allow you to select between the different displays.");
          }
        });

        compButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            String allSequencesVar = reader.getNTBasePairs();
            Map<Character,Integer> frequencies = new HashMap<>();
            for (char ch : allSequencesVar.toCharArray()) frequencies.put(ch, frequencies.getOrDefault(ch, 0) + 1);
            
  		  	ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
  	  	  	PrintStream old_out = System.out;
  	  	  	System.setOut(new PrintStream(pipeOut));
  		   	for (Map.Entry entry : frequencies.entrySet()) System.out.println(entry.getKey() + ", " + entry.getValue());
  
  		 	System.setOut(old_out);
  	  	  	String output = new String(pipeOut.toByteArray());
            textArea.setText(output);
            showTextField(false);
            showPieChart(true);
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
            textArea.setText("");
            textArea.setLineWrap(true);
            String formattedString = output.replaceAll("(.{120})", "$1\n").replaceAll(",\\s", "");
            textArea.append(formattedString);
            textArea.setCaretPosition(0);
            showTextField(true);
            showPieChart(false);
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
            textArea.setText("");
            String formattedString = output.replaceAll("(.{120})", "$1\n");
            textArea.append(formattedString);
            showTextField(true);
            showPieChart(false);
          }
        });

        XYDataset ds = createDataset();
        PieDataset dataset = createPieDataset();
        JFreeChart chart2 = ChartFactory.createPieChart("Nucleotide Distribution", dataset, true, true, false);
        JFreeChart chart = ChartFactory.createXYLineChart("Test Chart", "x", "y", ds, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel pieChart = new ChartPanel(chart2);
        ChartPanel cp = new ChartPanel(chart);
    	scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        frame.setLayout(experimentLayout);
        frame.add(ntSequenceButton);
        frame.add(summButton);
        frame.add(compButton);
        frame.add(helpButton);
        frame.add(exportButton);
        pieChartPanel.add(pieChart);
        frame.add(pieChartPanel);
    	frame.add(scroll);
		frame.add(cp);
        frame.setTitle("Open Reading Frame Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setVisible(true);

        textArea.setEditable(false);
        textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		pieChartPanel.setVisible(false);
      }
}

	private void showTextField(boolean show){
		    scroll.setVisible(show);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}

	private void showPieChart(boolean show){
			pieChartPanel.setVisible(show);
            frame.invalidate();
			frame.validate();
			frame.repaint();
	}


	private static PieDataset createPieDataset(){
	    DefaultPieDataset dataset = new DefaultPieDataset();
	    dataset.setValue("80-100", 120);
	    dataset.setValue("60-79", 80);
	    dataset.setValue("40-59", 20);
	    dataset.setValue("20-39", 7);
	    dataset.setValue("0-19", 3);
	    return dataset;
	}

	private static XYDataset createDataset(){
        DefaultXYDataset ds = new DefaultXYDataset();
        double[][] data = { {0.1, 1, 0.3}, {1, 2, 3} };
        ds.addSeries("series1", data);
        return ds;
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

  public double round(double value, int decimals) {
    double factor = (double) Math.pow(10, decimals);
    value *= factor;
    double tmp = Math.round(value);
    return (double) tmp / factor;
  }

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
    String path, header, aaSeq, ntSequenceString,line;
    int    lengthSum;
    ArrayList<OpenReadingFrame> ORFCollection = new ArrayList<OpenReadingFrame>();

    ORFCollector(String path) {
        this.path = path;
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

        //reg expression for ORF region matching: starts with ATG, then looks for multiple of 3 until end codon.
        Pattern checkRegex = Pattern.compile("ATG(?:[ATGC]{3}){20,200}?(?:TAA|TAG|TGA)");
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
      System.out.println(ORFCollection);
    }

    public void exportCollection(String path){
    	try{
    		PrintWriter csvWriter = new PrintWriter(new File(path));
    		StringBuilder sb = new StringBuilder();

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
      } catch (IOException e){
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