package com.car.cargo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartest")
public class TestController {
	@GetMapping
	public String sayHello() {
		return "Hello yassine";
	}

}
