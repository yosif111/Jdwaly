package com.jdwaly.demo.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParseController {
    @GetMapping("/parse")
    public String ParseCourses(){
     return "Hello"  ;
    }
}
