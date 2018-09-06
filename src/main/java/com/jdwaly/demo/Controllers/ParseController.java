package com.jdwaly.demo.Controllers;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jdwaly.demo.Models.College;
import com.jdwaly.demo.Models.Major;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


@RestController
public class ParseController {
    @GetMapping("/parse")
    public String ParseMajors(){

        ArrayList<College> colleges = new ArrayList<>();
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        HtmlPage page = null;
        try {
            String url = "https://edugate.ksu.edu.sa/ksu/ui/guest/timetable/index/scheduleTreeCoursesIndex.faces";
            page = client.getPage(url);
        }catch(Exception e){
            e.printStackTrace();
        }

        client.waitForBackgroundJavaScript(30 * 1000); //* will wait JavaScript to execute up to 30s since the web page html is dynamic   */

        DomNodeList<HtmlElement> data =  page.getElementById("dtree0").getElementsByTagName("div");

        for (int i=0; i< data.size(); i++){
            if(data.get(i).getChildElementCount() == 3) { // div that contains a college, thus it's majors are on the next div
                String CollegeName = data.get(i).getElementsByAttribute("a","class","node").get(0).asText();
                ArrayList<Major> majors = getMajors(data.get(i+1));
                colleges.add(new College(CollegeName, majors));
                i = i + majors.size(); // jump over the majors, no need to iterate over them.
            }
        }

        printColleges(colleges); // for testing
        return page.getElementById("dtree0").asXml();

    }

    public ArrayList<Major> getMajors(HtmlElement majorsDiv){
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

    }
