import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.awt.*;
import java.lang.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
*OpenReadingFrame Class
*@author Robin Kohze
*@Version 0.1 16-06-2018
*Description: class that holds the OpenReadingFrame properties and sequences.
*Properties : ntSequence, bpstart, bpEnd.
*Methods    : getSequence(); getBpStart(); getBpEnd(); getBpLength(); round(double, int); getGCPercentage(); 
*			  compareTo(); toString();
*/
public class OpenReadingFrame implements Comparable<OpenReadingFrame> {

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
