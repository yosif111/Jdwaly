package com.jdwaly.demo.Models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Schedule {
    ArrayList<Map<String, Integer>> scheduleList;

    public ArrayList<Section> getFittedSections() {
        return fittedSections;
    }

    ArrayList<Section> fittedSections;
    String[] timeSlots;
    //String[] timeSlots = {"08:00", "08:30","09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30",
    //        "14:00", "14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30","19:00"};
    Map<String, Integer> sunday;
    Map<String, Integer> monday;
    Map<String, Integer> tuesday;
    Map<String, Integer> wednesday;
    Map<String, Integer> thursday;

    public Schedule(Schedule s){
        this.scheduleList = s.scheduleList;
        this.fittedSections = s.fittedSections;
        this.timeSlots = s.timeSlots;
    }
    public Schedule(){
        scheduleList = new ArrayList<Map<String, Integer>>();
        fittedSections = new ArrayList<Section>();
        sunday = new LinkedHashMap<String, Integer>();
        monday = new LinkedHashMap<String, Integer>();
        tuesday = new LinkedHashMap<String, Integer>();
        wednesday = new LinkedHashMap<String, Integer>();
        thursday = new LinkedHashMap<String, Integer>();
        timeSlots = new String[]{"08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
                "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"};


        for (String timeSlot : timeSlots){
            sunday.put(timeSlot, -1);
            monday.put(timeSlot, -1);
            tuesday.put(timeSlot, -1);
            wednesday.put(timeSlot, -1);
            thursday.put(timeSlot, -1);
        }

        scheduleList.add(sunday);
        scheduleList.add(monday);
        scheduleList.add(tuesday);
        scheduleList.add(wednesday);
        scheduleList.add(thursday);

    }


    public void fitSection(Section section){

        if (section == null)
            return;

        for (int day : section.getDays()){
            if(scheduleList.get(day - 1).get(section.getStartTime()) == -1 && scheduleList.get(day - 1).get(section.getEndTime().substring(0, 2) + ":00") == -1 ){
               // System.out.println(section.getSectionNumber() + " Overwites " + scheduleList.get(day - 1).get(section.getStartTime()));
                scheduleList.get(day - 1).put(section.getStartTime(), Integer.parseInt(section.getSectionNumber()));
                scheduleList.get(day - 1).put(section.getEndTime().substring(0, 2) + ":00", Integer.parseInt(section.getSectionNumber()));

                fittedSections.add(section);
            }
        }

        if(section.getPractical().size() > 0)
            fitSection(section.getPractical().get(0));
        if (section.getTutorial().size() > 0)
            fitSection(section.getTutorial().get(0));


    }


    public boolean canBeFitted(Section section){

        if (section == null)
            return true;

        boolean lectureFlag = true;
        boolean practicalFlag = true;
        boolean tutorialFlag = true;

        for (int day : section.getDays()){
//            System.out.println("------------------------------------------------------------------------------------------------------------------------------");
//            System.out.println(section.getSectionNumber());
//            System.out.println("Day: "+ day);
//            System.out.println("Start Time: " +section.getStartTime());
//            System.out.println(section.getDays().get(0));
//            System.out.println("Practical Section: "+ section.getPractical().size());
//            System.out.println("Tutorial Section: "+ section.getTutorial().size());
            //System.out.println(section.getEndTime().substring(0, 2) + "00");

            if(scheduleList.get(day - 1).get(section.getStartTime()) != -1  || scheduleList.get(day - 1).get(section.getEndTime().substring(0, 2) + ":00") != -1){
                lectureFlag = false;
            }
        }
        if(section.getPractical().size() > 0) {
            practicalFlag = canBeFitted(section.getPractical().get(0));
        }
        if (section.getTutorial().size() > 0)
            tutorialFlag = canBeFitted(section.getTutorial().get(0));


        return lectureFlag && practicalFlag && tutorialFlag;

    }

    public long parseToMillis(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public void printSchedule(){

        for (int i=0; i< scheduleList.size(); i++){
            System.out.println("Day: " + i);
                for (String key: scheduleList.get(i).keySet()){
                    int value = scheduleList.get(i).get(key);
                    System.out.println(key +"     " + value);
                }
        }

    }


}


