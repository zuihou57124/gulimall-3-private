package com.project.gulimallorder.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/{page}.html")
    public String getPage(@PathVariable("page") String page){
        return page;
    }

}
