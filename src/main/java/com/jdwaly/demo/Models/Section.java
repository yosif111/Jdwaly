package com.jdwaly.demo.Models;

public class Section {

    private String sectionNumber;
    private String startTime;
    private String endTime;
    private String instructorName;

    public Section(String sectionNumber, String startTime, String endTime, String instructorName) {
        this.sectionNumber = sectionNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.instructorName = instructorName;
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
}
