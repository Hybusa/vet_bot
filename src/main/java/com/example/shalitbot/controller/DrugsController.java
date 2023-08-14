package com.example.shalitbot.controller;

import com.example.shalitbot.model.Drug;
import com.example.shalitbot.service.DrugsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("drugs")
public class DrugsController {
    private final DrugsService drugsService;

    public DrugsController(DrugsService drugsService) {
        this.drugsService = drugsService;
    }

    @PostMapping
    public ResponseEntity<Drug> createDrugEntry(@RequestBody Drug drug){
        return ResponseEntity.ok(drugsService.saveDrug(drug));
    }

    @GetMapping
    public ResponseEntity<List<Drug>> getAllDrugs(@RequestParam (required = false) String typeGroup){
        if(typeGroup != null)
            return ResponseEntity.ok(drugsService.findAllByTypeGroup(typeGroup));
        return ResponseEntity.ok(drugsService.findAll());
    }

    @PutMapping
    public ResponseEntity<Drug> updateDrugEntry(@RequestBody Drug drug){
        Optional<Drug> optDrug =  drugsService.updateEntry(drug);
        return optDrug.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Drug> deleteDrugById(@PathVariable Long id){
        if(drugsService.deleteEntry(id))
            return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }



}
