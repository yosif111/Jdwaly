package com.jdwaly.demo.Models;

import java.util.ArrayList;

public class College {
    private String name;
    private ArrayList<Major> majors;

    public College(String name, ArrayList<Major> majors){
        this.name = name;
        this.majors = majors;
    }


    public String getName() {
        return name;
    }

    public ArrayList<Major> getMajors() {
        return majors;
    }

    public void printMajors(){
        for (Major m : majors ){
            System.out.println(m.getName());
        }
    }

}
