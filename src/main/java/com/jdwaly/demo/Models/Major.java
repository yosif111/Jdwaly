package com.jdwaly.demo.Models;

public class Major {

    private String name;
    private Course[] courses;

    public Major(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Course[] getCourses() {
        return courses;
    }

    public void setCourses(Course[] courses) {
        this.courses = courses;
    }
}
