# ORFJava

A Java based project to identify genomwide all Open Reading Frames and expressing their stats. 

Java Projekt: An open reading frame viewer 

Input
sequence file or web API -  long array of nucleotide sequence

Interface
 - Sorting of ORF
 - Filtering of ORF
 - Show summary stats
 - GC/AT content
 - length
 - Epigenomic factors?
 - Promotors
 - Region specificity?
 - Select genome & species
Classes

Interface
 - starts interface
 - asks for input file
     
ORFCollection
 - arrayList
 - searches through genome and creates arrayList of OpenReadingFrame objects
 - calls sorting
 - calls filtering
     
OpenReadingFrame
 - hasSequence
 - has length	
 - has GC/AT
 - has epigenetic factors
 - has genetic region / bp number
