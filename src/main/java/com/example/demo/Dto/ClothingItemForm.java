package com.example.demo.Dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClothingItemForm {

    @NotBlank(message = "Nama item wajib diisi")
    private String name;

    @NotBlank(message = "Tipe item wajib dipilih")
    private String categoryName;

    private String color = "#000000";
    private String secondaryColor = "#333333";
    private String fit = "Regular";
    private String condition = "Good";
    private int timesWorn = 0;
    private boolean favorite = false;

    private int formalityLevel = 3;

    private List<Long> tagIds = new ArrayList<>();
}