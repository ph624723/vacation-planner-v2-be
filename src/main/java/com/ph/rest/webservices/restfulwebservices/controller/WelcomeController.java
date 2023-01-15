package com.ph.rest.webservices.restfulwebservices.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@ApiIgnore
//@RequestMapping("/test")
public class WelcomeController {

    @ApiOperation(value = "Basic redirect to bring new users to the home page")
    @GetMapping(value={"/","/welcome"})
    public String welcome(){
        //return new ResponseEntity<String>("Nothing to see here. Go to /swagger-ui.html for documentation of available endpoints.", HttpStatus.OK);
        return "redirect:/view/home";
    }
}
