package in.coelite.internal.portfolio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.coelite.internal.portfolio.dto.TestDto;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping({"/get/", "/get"})
    public ResponseEntity<String> checkGetHealth(){
        return new ResponseEntity<>("[GET] Server returned OK", HttpStatus.OK);
    }

    @PostMapping({"/post/", "/post"})
    public ResponseEntity<String> checkPostHealth(@RequestBody TestDto testDto){
        return new ResponseEntity<>("[POST] Server received OK\nTest ID:"+testDto.getTestId(), HttpStatus.OK);
    }
}
