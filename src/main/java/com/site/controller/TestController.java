package com.site.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/creator/test")
    public boolean test() {
        return true;
    }

    @GetMapping("/api/admin/test")
    public boolean test2() {
        return false;
    }
}

