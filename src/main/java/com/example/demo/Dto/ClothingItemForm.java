package com.example.demo.Dto;

import org.springframework.web.multipart.MultipartFile;

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

    @NotBlank(message = "Kategori wajib dipilih")
    private String category; // Atasan, Bawahan, Luaran, Sepatu, Aksesori, Tas, Lainnya

    private MultipartFile image; // foto pakaian

    private boolean favorite = false;
}