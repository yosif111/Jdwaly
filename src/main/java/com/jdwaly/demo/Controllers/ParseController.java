package com.jdwaly.demo.Controllers;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jdwaly.demo.Models.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
public class ParseController {

    ArrayList<College> colleges;
    ArrayList<Course> courses;
    WebClient client;
    HtmlPage page;
    String url;
    ArrayList<Schedule> candidateSchedules = new ArrayList<>();

    public ParseController(){
        colleges = new ArrayList<>();
        courses = new ArrayList<>();
        // set up the web client
        client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        page = null;
    }


    //ParseMajors returns a list of colleges and majors
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ArrayList<College> ParseCollegesAndMajors(){

        //fetch the html page containing the Colleges and majors
        try {
            String url = "https://edugate.ksu.edu.sa/ksu/ui/guest/timetable/index/scheduleTreeCoursesIndex.faces";
            page = client.getPage(url);
        }catch(Exception e){
            e.printStackTrace();
        }

        client.waitForBackgroundJavaScriptStartingBefore(30 * 1000); //* will wait JavaScript to execute up to 30s since the web page html is dynamic   */

        DomNodeList<HtmlElement> data =  page.getElementById("dtree0").getElementsByTagName("div"); // contains the colleges and majors div

        // get the colleges and majors.
         colleges = getColleges(data);

         return colleges;
    }


    // given a major name, returns a list of all courses for that major.
    @CrossOrigin
    @RequestMapping(value= "/courses", method= RequestMethod.POST)
    private ArrayList<Course> getMajorCourses(@RequestBody  Map<String, Object> payload){

        if (colleges.size() == 0) // this happens when this method is invoked before the first one, i did this just for testing;
            ParseCollegesAndMajors();

        HtmlPage pageCopy = this.page;

        DomElement d =  pageCopy.getAnchorByText(payload.get("major").toString());

        try {
            pageCopy = d.click();
            client.waitForBackgroundJavaScriptStartingBefore(2 * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        // contains the html 'tr' elements of all the courses
        DomNodeList<HtmlElement> coursesRows = pageCopy.getElementsById("myForm:timetable").get(0).getElementsByTagName("tbody").get(0).getElementsByTagName("tr");

        // fills the courses array list with the selected major courses
        parseMajorCoursesFromHtml(coursesRows);

        return courses;
    }


    @CrossOrigin
    @RequestMapping(value= "/getSchedules", method= RequestMethod.POST)
    public ArrayList<Schedule> getAllPermutationsOfSchedules(@RequestBody  Map<String, Object> payload){

        if (courses.size() == 0) // this method should not be invoked first;
            return null;

        List<Integer> sectionNumbers = (List<Integer>) payload.get("sections");

        ArrayList<Section> chosenSections = new ArrayList<>();

        for (Integer i : sectionNumbers){
            chosenSections.add(getSectionFromSectionNumber(i));
        }

        //chosenSections = handleSectionsWithMoreThanOnePracticalSection(chosenSections);

        fixTimesBeforeSorting(chosenSections);

        ArrayList<String> fittedCourses; // contains a list of the fitted courses
        ArrayList<String> courses = new ArrayList<>(); // contains the list of the courses to register

        for (Section s: chosenSections){
            if(!courses.contains(s.getCode()))
                courses.add(s.getCode());
        }

        List<List<Integer>> powerSet = new LinkedList<List<Integer>>();
        long startTime = System.nanoTime();
        powerSet.addAll(combination(sectionNumbers, courses.size()));

        for (int i=0; i< powerSet.size(); i++) {

            Schedule s = new Schedule();
            fittedCourses = new ArrayList<>();

            for (int j = 0; j < powerSet.get(i).size(); j++) {

                Section section = getSectionFromSectionNumber(powerSet.get(i).get(j));

                if (s.canBeFitted(section) && !fittedCourses.contains(section.getCode())) {
                    s.fitSection(section);
                    fittedCourses.add(section.getCode());
                }

                if (fittedCourses.size() == courses.size()){ // this schedule contains all the required courses.
                    candidateSchedules.add(s);
                    break;
                }
            }

        }

        for (Schedule s : candidateSchedules){
            s.printSchedule();
        }

        System.out.println(candidateSchedules.size());
        return candidateSchedules;
    }


    // given html 'tr' elements, returns a list of the courses
    private void parseMajorCoursesFromHtml(DomNodeList<HtmlElement> coursesRows){

        for (HtmlElement courseRow : coursesRows) {

            DomNodeList<HtmlElement> cells = courseRow.getElementsByTagName("td");

            String courseCode = cells.get(0).asText();
            String courseName = cells.get(1).asText();

            Course course = addNewCourseOrGetExistingOne(courseCode, courseName);

            String sectionNumber = cells.get(2).asText();
            String sectionType = cells.get(3).asText();
//            String creditHours = cells.get(4).asText();

            // contains the instructor name, days, and start and end times.
            DomNodeList<HtmlElement> detailsToolTip = cells.get(7).getElementsByTagName("input");

            String instructorsName = detailsToolTip.get(0).getAttribute("value");
            //TODO: time data for 2 4 courses is different, handle that.
            String timeData = detailsToolTip.get(1).getAttribute("value");

            if(timeData.isEmpty()) // some courses don't have a time (e.g. Graduation or research courses, I don't see any need to include them)
                continue;

            //contains a list of days of the course, e.g. a course on sunday and on monday days = [1, 2]
            ArrayList<Integer> days = getCourseDaysFromUnfilteredData(timeData);

            timeData = timeData.trim().substring(timeData.indexOf("t") + 1); // cut the days part to get the start and end time.

            String[] timeDataArr = timeData.split(" ");
            String startTime = timeDataArr[0];
            String endTime = timeDataArr[3];

            Section section = new Section(courseName, courseCode, sectionNumber, sectionType, startTime, endTime, instructorsName, days);

            if(section.getType().equals("محاضرة")){
                course.addSection(section); // add this lecture section directly to the course
            }else if (section.getType().equals("عملي")){

                Section tmpSectionRef = courses.get(courses.size() - 1).getLatestLectureSection(); // append it to it's lecture course
                if(tmpSectionRef == null) // this is a course that doesn't belong to any lecture course (e.g. labs), add it directly to the courses
                    courses.get(courses.size() - 1).addSection(section);
                else
                    tmpSectionRef.addPractical(section);
            }else if(section.getType().equals("تمارين")){
                courses.get(courses.size() - 1).getLatestLectureSection().addTutorial(section);// append it to it's lecture course
            }

        }
    }

    // purpose is to register new courses or return the reference if it's already registered.
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



    // given a set of n integers, find all possible combinations of a k pairs
    // in our case n is the number of chosen sections, k is the number of courses wanted to register. PS: a did not write this.
    public static <T> List<List<T>> combination(List<T> values, int size) {

        if (size == 0) {
            return Collections.singletonList(Collections.<T> emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combination(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combination(subSet, size));

        return combination;
    }


    // handles sections with more than one practical section that students can choose one from, For example a Java Programming 1 course has 2 practical sections
    // a student can choose either of them at the time of registration, to solve this make n copies of the section for n practical sections
    private ArrayList<Section> handleSectionsWithMoreThanOnePracticalSection(ArrayList<Section> unhandledChosenSections){

        ArrayList<Section> handledSections = new ArrayList<>();

        for (Section section : unhandledChosenSections){

            if(section.getPractical().size() > 1){  // has more than one practical sections

                ArrayList<Section> practicals = section.getPractical(); // copy of all practical sections

                for (int i=0 ; i< practicals.size(); i++) {

                    Section s = new Section(section.getName(), section.getCode(), section.getSectionNumber(), section.getType(),
                            section.getStartTime(), section.getEndTime(), section.getInstructorsName(), section.getDays());

                    s.addPractical(practicals.get(i)); // insert only one
                    s.setTutorial(section.getTutorial()); // keep the tutorial as it is
                    handledSections.add(s);
                }
            } else{
                handledSections.add(section);
            }

        }
        return handledSections;
    }

    private void fixTimesBeforeSorting(ArrayList<Section> chosenSections){
       ArrayList<Section> fixedChosenSection = new ArrayList<>();

       for (Section section : chosenSections){

           if (section.getPractical().size() > 0) // recursively go through the practical sections
               fixTimesBeforeSorting(section.getPractical());

           if (section.getTutorial().size() > 0)// recursively go through the tutorial sections
               fixTimesBeforeSorting(section.getTutorial());

           int startTime = Integer.parseInt(section.getStartTime().substring(0, 2));
           //System.out.println(section.getStartTime());

           if( startTime < 8 ){
               section.setStartTime( (startTime + 12) + "" + section.getStartTime().substring(2));
               int endTime = Integer.parseInt(section.getEndTime().substring(0, 2));
               section.setEndTime(( endTime + 12) + "" + section.getEndTime().substring(2));
           }

          // System.out.println("Start Time: " + section.getStartTime());
       }
    }


    // get majors of a college.
    private ArrayList<Major> getMajors(HtmlElement majorsDiv){

       DomNodeList<HtmlElement> innerDivs =  majorsDiv.getElementsByTagName("div"); // inner divs each containing a major
        ArrayList<Major> majors = new ArrayList<>();

       for (HtmlElement div : innerDivs){
           String majorName = div.getElementsByTagName("a").get(0).asText();
           majors.add(new Major(majorName));
       }
       return majors;
    }

    // given a string containing info about the days of the section, start and end time and instructor name, return a list
    private ArrayList<Integer> getCourseDaysFromUnfilteredData(String timeData) {

        ArrayList<String> tmpArr = new ArrayList<>();
        Collections.addAll(tmpArr, timeData.trim().split(" "));

        ArrayList<Integer> days = new ArrayList<>();

        if(tmpArr.size() > 22) { // these types of courses are outliers

            int i = 0;
            while (!tmpArr.get(i).equals("@t")) {
                days.add(Integer.parseInt(tmpArr.get(i)));
                i++;
            }
            days.add(Integer.parseInt(tmpArr.get(tmpArr.lastIndexOf("@t") -1 )));
        }
        else {
            int i = 0;
            while (!tmpArr.get(i).equals("@t")) {
                days.add(Integer.parseInt(tmpArr.get(i)));
                i++;
            }
        }
        return days;
    }


    // given a section number, iterate the course list and return that section
    public Section getSectionFromSectionNumber(int sectionNumber){
        for (Course c : courses){
             for (Section s : c.getSections()){
                 if(Integer.parseInt(s.getSectionNumber()) == sectionNumber)
                     return s;
             }

        }
        System.out.println("Section number not found: " + sectionNumber);
        return null;
    }

//    public void printColleges(ArrayList<College> colleges) {
//        for (College c : colleges){
//            System.out.println();
//            System.out.println();
//
//            System.out.println("-----------------------------------------------------");
//            System.out.println("College Name:" + " " + c.getName());
//            System.out.println("number of majors: " + c.getMajors().size());
//            c.printMajors();
//            System.out.println("-----------------------------------------------------");
//
//        }
//    }


}

