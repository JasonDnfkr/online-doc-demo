package com.example.online_doc.controller;

import com.example.online_doc.server.DocWebSocketServer;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebSocketController {
    @Autowired
    private DocWebSocketServer docWebSocketServer;

    @GetMapping("/index")
    public ModelAndView index() {
        return new ModelAndView("/index");
    }

    @GetMapping("/webSocket")
    public ModelAndView socket() {
        ModelAndView mav = new ModelAndView("/webSocket");
        return mav;
    }

}
