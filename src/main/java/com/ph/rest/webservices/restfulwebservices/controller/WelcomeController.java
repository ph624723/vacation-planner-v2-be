package com.ph.rest.webservices.restfulwebservices.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
//@RequestMapping("/test")
public class WelcomeController {

    @ApiOperation(value = "Basic redirect to bring new users to the Swagger doc")
    @GetMapping(value={"/","/welcome"})
    public String welcome(){
        //return new ResponseEntity<String>("Nothing to see here. Go to /swagger-ui.html for documentation of available endpoints.", HttpStatus.OK);
        return "redirect:/view/home";
    }
}
