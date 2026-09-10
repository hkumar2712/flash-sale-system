package com.hims.flashsale.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController = @Controller + @ResponseBody.
 * It tells Spring: "instantiate this class as a bean, and whatever its methods
 * return should be written directly into the HTTP response body (as JSON/plain text),
 * not resolved to a view template (like a JSP/Thymeleaf page)."
 */
@RestController
public class HelloController {

    // @GetMapping("/hello") maps HTTP GET requests on /hello to this method.
    // Spring's DispatcherServlet (auto-configured because of spring-boot-starter-web)
    // receives the raw HTTP request, matches the URL pattern, and routes it here.
    @GetMapping("/hello")
    public String hello() {
        return "Hello from User Service - Flash Sale System is alive!";
    }
}
