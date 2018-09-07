package com.jdwaly.demo.Models;

import java.util.ArrayList;

public class Section extends Course {

    private String sectionNumber;
    private String type; // lecture or practical
    private String startTime;
    private String endTime;
    private String instructorsName;
    private ArrayList<Section> practical;
    private ArrayList<Section> tutorial;
    private ArrayList<Integer> days;

    public Section(String courseName, String courseCode, String sectionNumber,String type, String startTime, String endTime, String instructorName, ArrayList<Integer> days) {
        super(courseName, courseCode);
        this.sectionNumber = sectionNumber;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.instructorsName = instructorName;
        this.days = days;

        practical = new ArrayList<>();
        tutorial = new ArrayList<>();
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public String getInstructorsName() {
        return instructorsName;
    }

    public ArrayList<Integer> getDays() {
        return days;
    }

    public ArrayList<Section> getPractical() {
        return practical;
    }

    public void addPractical(Section practical) {
        this.practical.add(practical);
    }

    public String getType() {
        return type;
    }

    public ArrayList<Section> getTutorial() {
        return tutorial;
    }

    public void addTutorial(Section tutorial) {
        this.tutorial.add(tutorial);
    }

    public void print(){
        //super.print();
        System.out.println("    Section Number: "+ this.getSectionNumber());
        System.out.println("    Type: "+ this.getType());
//        System.out.println("Start Time: "+ this.getStartTime());
//        System.out.println("End Time: "+ this.getEndTime());
//        System.out.println("Instructor Name "+ this.getInstructorsName());
//        System.out.println("Days "+ this.getDays());

        for (Section section : practical){
           // section.print();
            System.out.println("                    Section Number: "+ section.getSectionNumber());
            System.out.println("                    Type: "+ section.getType());
        }
        for (Section section : tutorial){
           // section.print();
            System.out.println("                    Section Number: "+ section.getSectionNumber());
            System.out.println("                    Type: "+ section.getType());
        }
    }

}

