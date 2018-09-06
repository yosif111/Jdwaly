package com.jdwaly.demo.Models;

public class Course {

    private String FullName;
    private String name;
    private String code;
    private Section[] sections;

    public Course(String fullName, Section[] sections) {
        FullName = fullName;
        this.sections = sections;
    }

    public Section[] getSections() {
        return sections;
    }

    public String getFullName() {
        return FullName;
    }
}
