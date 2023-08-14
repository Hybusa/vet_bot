package com.example.shalitbot.service;

import com.example.shalitbot.model.Drug;
import com.example.shalitbot.repository.DrugsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DrugsService {
    private final DrugsRepository drugsRepository;

    public DrugsService(DrugsRepository drugsRepository) {
        this.drugsRepository = drugsRepository;
    }


    public Drug saveDrug(Drug drug) {
        return drugsRepository.save(drug);
    }

    public List<Drug> findByNameLike(String name) {
        List<Drug> drugList;
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        String trimmedName = StringUtils.trim(name);

        if (name.matches("^[A-Za-z]+$")) {
            drugList = getAllDrugStream()
                    .filter(d -> d.getNameLat().equalsIgnoreCase(trimmedName))
                    .collect(Collectors.toList());

            if (drugList.size() > 0)
                return drugList;

            drugList = getAllDrugStream()
                    .filter(d -> levenshteinDistance.apply(d.getNameLat(), trimmedName) < 5)
                    .collect(Collectors.toList());
        } else {
            drugList = getAllDrugStream()
                    .filter(d -> d.getNameRus().equalsIgnoreCase(trimmedName))
                    .collect(Collectors.toList());

            if (drugList.size() > 0)
                return drugList;

            drugList = getAllDrugStream()
                    .filter(d -> levenshteinDistance.apply(d.getNameRus(), trimmedName) < 5)
                    .collect(Collectors.toList());
        }
        return drugList;
    }

    private Stream<Drug> getAllDrugStream() {
        return drugsRepository.findAll().parallelStream();
    }

    public List<Drug> findAll() {
        return drugsRepository.findAll();
    }

    public boolean deleteEntry(Long id) {
        if (drugsRepository.existsById(id)) {
            drugsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Drug> findByName(String drugName) {
        return drugsRepository.findByNameLat(drugName);
    }


    public List<Drug> findAllByTypeGroup(String typeGroup) {
        return drugsRepository.findAllByTypeGroupIgnoreCase(typeGroup);
    }

    public Optional<Drug> updateEntry(Drug drug) {
        if (drugsRepository.existsById(drug.getId())) {
            return Optional.of(drugsRepository.save(drug));
        }
        return Optional.empty();
    }

    public List<String> getTypeGroupsAsList() {
        return drugsRepository.getTypeGroups();
    }
}

