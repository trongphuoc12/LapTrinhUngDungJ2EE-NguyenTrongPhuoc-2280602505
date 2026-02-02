package com.example.bai3.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.bai3.Services.BookService;


@Controller
public class HomeController {
    @Autowired
    private BookService bookService;
    
    @GetMapping({"/home","/"})
    public String Index(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "books";
    }
 
    
    
}
