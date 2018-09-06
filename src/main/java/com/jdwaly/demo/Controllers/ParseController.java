package com.jdwaly.demo.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.jsoup.*;
//import org.jsoup.helper.*;
//import org.jsoup.nodes.*;
//import org.jsoup.select.*;


@RestController
public class ParseController {
    @GetMapping("/parse")
    public String ParseCourses(){
//        Document doc = Jsoup.connect("http://example.com/").get();
//        String title = doc.title();
        return "Hiii";
    }
}
