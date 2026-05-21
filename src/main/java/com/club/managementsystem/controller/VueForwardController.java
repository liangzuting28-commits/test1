package com.club.managementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class VueForwardController {

    @GetMapping("/vue/{path:[^\\.]*}")
    public String forward(@PathVariable String path) {
        return "forward:/vue/index.html";
    }

    @GetMapping("/vue")
    public String forwardRoot() {
        return "forward:/vue/index.html";
    }
}
