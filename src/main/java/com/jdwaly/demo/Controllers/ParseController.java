package com.jdwaly.demo.Controllers;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jdwaly.demo.Models.College;
import com.jdwaly.demo.Models.Course;
import com.jdwaly.demo.Models.Major;
import com.jdwaly.demo.Models.Section;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class ParseController {

    ArrayList<College> colleges;
    ArrayList<Course> courses;
    WebClient client;
    HtmlPage page;
    String url;

    public ParseController(){
        colleges = new ArrayList<>();
        courses = new ArrayList<>();
        client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        page = null;
    }


    @GetMapping("/parse")
    public String ParseMajors(){

        // set up the web client



        //fetch the html page containing the Colleges and majors
        try {
            String url = "https://edugate.ksu.edu.sa/ksu/ui/guest/timetable/index/scheduleTreeCoursesIndex.faces";
            page = client.getPage(url);
        }catch(Exception e){
            e.printStackTrace();
        }

        client.waitForBackgroundJavaScriptStartingBefore(30 * 1000); //* will wait JavaScript to execute up to 30s since the web page html is dynamic   */

        DomNodeList<HtmlElement> data =  page.getElementById("dtree0").getElementsByTagName("div"); // contains the colleges and majors

        // get the colleges and majors
         colleges = getColleges(data);

        //printColleges(colleges); // for testing
        getMajorCourses("SWEN-هندسة البرمجيات");


        //return page.getElementsById("myForm:timetable").get(0).getElementsByTagName("tbody").get(0).asXml() + "";

       return page.asXml();

    }

    private Course addNewCourseOrGetExistingOne(String courseCode, String courseName){
        List<Course> s = courses.stream().filter(c-> c.getName().equals(courseName)).collect(Collectors.toList());
        if(s.size() > 0 ) // the course exists and was registered previously, return its reference
            return s.get(0);
        else
            courses.add(new Course(courseName, courseCode)); // register the course
        return courses.get(courses.size() - 1); // return the newly registered course which is the last element;

    }

    private ArrayList<College> getColleges(DomNodeList<HtmlElement> data){
        ArrayList<College> colleges = new ArrayList<>();
        for (int i=0; i< data.size(); i++){
            if(data.get(i).getChildElementCount() == 3) { // div that contains a college, thus it's majors are on the next div
                String CollegeName = data.get(i).getElementsByAttribute("a","class","node").get(0).asText();
                ArrayList<Major> majors = getMajors(data.get(i+1));
                colleges.add(new College(CollegeName, majors));
                i = i + majors.size(); // jump over the majors, no need to iterate over them.
            }
        }
        return colleges;
    }

    private void getMajorCourses(String majorName){

        DomElement d =  page.getAnchorByText("SWEN-هندسة البرمجيات");

        try {
            page = d.click();
            client.waitForBackgroundJavaScriptStartingBefore(2 * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        DomNodeList<HtmlElement> coursesRows = page.getElementsById("myForm:timetable").get(0).getElementsByTagName("tbody").get(0).getElementsByTagName("tr");


        for (HtmlElement courseRow : coursesRows) {

            DomNodeList<HtmlElement> cells = courseRow.getElementsByTagName("td");
            String courseCode = cells.get(0).asText();
            String courseName = cells.get(1).asText();

            Course course = addNewCourseOrGetExistingOne(courseCode, courseName);

            String sectionNumber = cells.get(2).asText();
            String sectionType = cells.get(3).asText();
            //String creditHours = cells.get(4).asText();

            DomNodeList<HtmlElement> detailsToolTip = cells.get(7).getElementsByTagName("input");

            String instructorsName = detailsToolTip.get(0).getAttribute("value");
            String timeData = detailsToolTip.get(1).getAttribute("value");

            if(timeData.isEmpty()) // some courses don't have a time (e.g. Graduation or research courses, I don't see any need to include them)
                continue;

            ArrayList<Integer> days = getCourseDaysFromUnfilteredData(timeData);
            String startTime = timeData.trim().split(" ")[2];
            String endTime = timeData.trim().split(" ")[5];

            Section section = new Section(courseName, courseCode, sectionNumber, sectionType, startTime, endTime, instructorsName, days);

            if(section.getType().equals("محاضرة")){
                course.addSection(section); // add this lecture section directly to the course
            }else if (section.getType().equals("عملي")){  // add it to its lecture section

                Section tmpSectionRef = courses.get(courses.size() - 1).getLatestLectureSection();
                if(tmpSectionRef == null) // this is a course that doesn't belong to any lecture course (e.g. labs), add it directly to the courses
                    courses.get(courses.size() - 1).addSection(section);
                else
                    tmpSectionRef.addPractical(section);
            }else if(section.getType().equals("تمارين")){  // add it to its lecture section
                courses.get(courses.size() - 1).getLatestLectureSection().addTutorial(section);
            }

        }


        for (Course c : courses){
            c.print();
        }

    }

    private ArrayList<Major> getMajors(HtmlElement majorsDiv){
       DomNodeList<HtmlElement> innerDivs =  majorsDiv.getElementsByTagName("div"); // inner divs each containing a major
        ArrayList<Major> majors = new ArrayList<>();

       for (HtmlElement div : innerDivs){
           String majorName = div.getElementsByTagName("a").get(0).asText();
           majors.add(new Major(majorName));
       }
       return majors;
    }

    public void printColleges(ArrayList<College> colleges) {
        for (College c : colleges){
            System.out.println();
            System.out.println();

            System.out.println("-----------------------------------------------------");
            System.out.println("College Name:" + " " + c.getName());
            System.out.println("number of majors: " + c.getMajors().size());
            c.printMajors();
            System.out.println("-----------------------------------------------------");

        }
    }

    private ArrayList<Integer> getCourseDaysFromUnfilteredData(String timeData) {

        String[] tmpArr = timeData.trim().split(" ");
        ArrayList<Integer> days = new ArrayList<>();

        int i = 0;
        while (!tmpArr[i].equals("@t")) {
            days.add(Integer.parseInt(tmpArr[i]));
            i++;
        }
        return days;
    }

}


//    // spans in the pagination pane.
//    DomNodeList<HtmlElement> paginationSpans = page.getElementById("pag").getElementsByTagName("span");
//
//        for(HtmlElement span: paginationSpans){
//                if(span.getElementsByAttribute("span", "class", "ui-icon ui-icon-seek-next").size() > 0){
//
//                // System.out.println();
//                try {
//                page = span.click();
//                //client.waitForBackgroundJavaScriptStartingBefore(30 * 1000);
//
//                } catch (Exception e) {
//                System.out.println("Javascript Error");
//                } catch (Error e) {
//                System.out.println("Javascript Error");
//                }
//
//                System.out.println("Finished the loop");
//                System.out.println("Finished the loop");
//                System.out.println("Finished the loop");
//                System.out.println("Finished the loop");
//                }
//                }