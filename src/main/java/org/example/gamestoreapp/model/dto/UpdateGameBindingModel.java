package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.*;
import org.example.gamestoreapp.model.enums.GenreName;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateGameBindingModel {
    private Long id;
    @Size(min = 3, max = 50, message = "Game title must be between 3 and 50 characters!")
    private String title;
    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters!")
    private String description;
    @Pattern(regexp = "^(http(s?):)([/|.\\w\\s:])*\\.(?:jpg|jpeg|png)(\\?\\w+=\\w+(&\\w+=\\w+)*)?$", message = "Image URL must be a valid image format!")
    private String imageUrl;
    private String videoUrl;
    @Past
    private LocalDate releaseDate;
    @NotBlank
    private String publisher;
    @NotNull
    private String genre;
    @PositiveOrZero
    private BigDecimal price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
