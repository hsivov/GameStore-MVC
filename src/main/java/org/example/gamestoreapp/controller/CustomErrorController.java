package org.example.gamestoreapp.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(WebRequest webRequest, Model model) {
        // Retrieve error attributes, including exception details
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(webRequest,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE));

        // Add attributes to the model to display them on the error page
        model.addAttribute("errorAttributes", errorAttributes);

        Object status = errorAttributes.get("status");
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        if (statusCode == 401) {
            return "error/401";
        } else if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 405) {
            return "error/405";
        } else if (statusCode == 500) {
            return "error/500";
        }

        return "error";
    }
}
