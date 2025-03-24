package com.example.demo.controller;


import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Data;
import com.example.demo.model.OutputData;
import com.example.demo.service.OptimizeService;

@RestController
@RequestMapping("/api")
public class OptimizationController {
	

	private final OptimizeService service;
	
	
	@Autowired
	public OptimizationController(OptimizeService service) {
		this.service = service;
	}
	
	@PostMapping("/optimize")
    public OutputData handleOptimization(@RequestBody Data data, Model model) {
		OutputData result = service.optimize(data);
	    if (Objects.isNull(result)) {
	        throw new RuntimeException("Optimization failed");
	    }

	    
	    return result;
    }
}
