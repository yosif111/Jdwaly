package com.jdwaly.demo.Models;

import java.util.ArrayList;

public class Course {

    protected String name;
    protected String code;
    private ArrayList<Section> sections;

    public Course(String name, String code) {
        this.name = name;
        this.code = code;
        sections = new ArrayList<>();
    }

    public ArrayList<Section> getSections() {
        return sections;
    }
    public void addSection(Section s){
        this.sections.add(s);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Section getLatestLectureSection(){
        if(sections.size() == 0 ) // this is a course that is not attached to a lecture course, return null
            return null;
        return sections.get(sections.size() - 1);
    }

    public void print(){
        System.out.println("---------");
        System.out.println("Course Name:" + this.getName());
        System.out.println("Course code:" + this.getCode());
        System.out.println("Sections: ");
        System.out.println();

        for (Section section: sections){
            section.print();
        }
    }
}


