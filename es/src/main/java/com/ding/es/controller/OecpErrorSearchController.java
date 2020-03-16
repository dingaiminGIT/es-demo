package com.ding.es.controller;

import com.ding.es.document.OecpError;
import com.ding.es.service.OecpErrorSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
public class OecpErrorSearchController {

    @Autowired
    private OecpErrorSearchService service;

    @GetMapping("/test")
    public String test() {
        return "Success";
    }

    @PostMapping("/create/index")
    public ResponseEntity createIndex() throws Exception {
        return new ResponseEntity(service.createErrorCodeIndex(), HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public ResponseEntity createErrorCode(@RequestBody OecpError document) throws Exception {
        return new ResponseEntity(service.createOrUpdateErrorCode(document), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public OecpError findById(@PathVariable String id) throws Exception {
        return service.findById(id);
    }

    @GetMapping
    public List<OecpError> findAll() throws Exception {
        return service.findAll();
    }

    @GetMapping(value = "/suggest")
    public List<OecpError> suggest(@RequestParam(value = "info") String info) throws Exception {
        return service.suggestErrorCode(info);
    }

    @GetMapping(value = "/search")
    public List<OecpError> searchByName(@RequestParam(value = "info") String info) throws Exception {
        //return service.findProfileByName(info);
        return service.searchErrorCode(info);
    }

    @DeleteMapping("/{id}")
    public String deleteErrorCode(@PathVariable String id) throws Exception {
        return service.deleteErrorCode(id);
    }
}
