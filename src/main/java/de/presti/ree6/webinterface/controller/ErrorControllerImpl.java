package de.presti.ree6.webinterface.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorControllerImpl implements org.springframework.boot.web.servlet.error.ErrorController {

    /**
     * Error Attributes in the Application
     */
    private final ErrorAttributes errorAttributes;
    private WebRequest webRequest;

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
    public String error(HttpServletRequest request, WebRequest webRequest, Model model) {
        Object status = model.getAttribute("errorCode") != null ? model.getAttribute("errorCode") : request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);;

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // display specific error page
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404/index";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403/index";
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return "error/400/index";
            } else if (statusCode != HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500/index";
            }
        }

        return "error/index";
    }
}
