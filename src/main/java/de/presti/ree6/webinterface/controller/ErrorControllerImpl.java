package de.presti.ree6.webinterface.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

@Controller
public class ErrorControllerImpl implements org.springframework.boot.web.servlet.error.ErrorController {

    /**
     * Error Attributes in the Application
     */
    private final ErrorAttributes errorAttributes;

    /**
     * Controller for the Error Controller
     * @param errorAttributes Attributes that give more Info about the Error.
     */
    public ErrorControllerImpl(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Request mapper for errors.
     * @return {@link String} for Thyme to the HTML Page.
     */
    @GetMapping(value = "/error")
    public String error(WebRequest webRequest) {
        return "error/index";
    }
}
