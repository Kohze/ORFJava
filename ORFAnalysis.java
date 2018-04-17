import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.awt.*;
import java.lang.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
*@author Robin Kohze
*@version 0.11 17-04-2018
*@see https://github.com/Kohze/ORFJava
*@see To run the programm the jFreeChart library (>1.023) required: http://www.jfree.org/jfreechart/
*The ORFAnalysis tool. It takes a given .txt file as input and calculates all theoretically possible ORFs.
*ORFAnalysis is the main class.
*Repository: https://github.com/Kohze/ORFJava
*Email  : Robin@Kohze.com
*/
public class ORFAnalysis {
  public static void main(String[] args) {
    UIContainer mainDisplay = new UIContainer();
    mainDisplay.startDisplay();
  }
}
