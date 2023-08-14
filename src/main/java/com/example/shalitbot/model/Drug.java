package com.example.shalitbot.model;


import javax.persistence.*;

@Entity
@Table(name ="drugs")
public class Drug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameLat;
    private String nameRus;
    private String typeGroup;
    private String dosageForCats;
    private String dosageForDogs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameLat() {
        return nameLat;
    }

    public void setNameLat(String nameLat) {
        this.nameLat = nameLat;
    }

    public String getNameRus() {
        return nameRus;
    }

    public void setNameRus(String nameRus) {
        this.nameRus = nameRus;
    }

    public String getTypeGroup() {
        return typeGroup;
    }

    public void setTypeGroup(String typeGroup) {
        this.typeGroup = typeGroup;
    }

    public String getDosageForCats() {
        return dosageForCats;
    }

    public void setDosageForCats(String dosageForCats) {
        this.dosageForCats = dosageForCats;
    }

    public String getDosageForDogs() {
        return dosageForDogs;
    }

    public void setDosageForDogs(String dosageForDogs) {
        this.dosageForDogs = dosageForDogs;
    }
}
