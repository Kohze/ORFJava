import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.awt.*;
import java.lang.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
*Sequence ORFCollector
*@author Robin Kohze
*@Version 0.25 17-04-2018
*Description: Reads sequence containing .txt file with path input and stores it within an ArrayList of OpenReadingFrame objects
*Properties : path, minORFLength,maxORFLength
*Methods    : getORFArray(); getORFStatistics(); returnORFCollection(); returnBasePairs(); exportORFCollection();
*			  printSorted();
*/
public class ORFCollector {
    String path, header, aaSeq, fullSequence, line, regexPattern;
    int    lengthSum, minORFLength, maxORFLength;
    ArrayList<OpenReadingFrame> ORFCollection = new ArrayList<OpenReadingFrame>();

    ORFCollector(String path, Integer minORFLength, Integer maxORFLength) {
        this.path = path;
        this.regexPattern = "ATG(?:[ATGC]{3}){" + minORFLength +","+ maxORFLength + "}?(?:TAA|TAG|TGA)";
    }
    
     public void getORFArray(){
      try{
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        ArrayList<OpenReadingFrame> ORFCollection = new ArrayList<OpenReadingFrame>();

        //Line wise reading of sequence data
        StringBuilder aaSeq = new StringBuilder();
        while ((line = reader.readLine()) != null) aaSeq.append(line); 
        fullSequence = aaSeq.toString();
        this.lengthSum += fullSequence.length();

        //regExpression for the ORF region matching.
        Pattern checkRegex = Pattern.compile(regexPattern);
		Matcher regexMatcher = checkRegex.matcher(fullSequence);
		while (regexMatcher.find()){
			if (regexMatcher.group().length() != 0){
				OpenReadingFrame ORFobject  = new OpenReadingFrame(regexMatcher.group(), regexMatcher.start(), regexMatcher.end());
				//adding Open Reading Frame objects to arrayList "ORFCollection"
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

    //functions to return the ORFCollector properties
    public void printSorted(){
      Collections.sort(ORFCollection);
      System.out.print(ORFCollection);
    }

    public ArrayList<OpenReadingFrame> returnORFCollection(){
    	return this.ORFCollection;
    }

    public String returnBasePairs(){
      return fullSequence;
    }

    //Getting overall statistics of the ORFCollection ArrayList
    public void getORFStatistics(){
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

    /*export function that creates .csv file
    *@param path A output path the .csv file is written to.
	**/
    public void exportORFCollection(String path){
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
}