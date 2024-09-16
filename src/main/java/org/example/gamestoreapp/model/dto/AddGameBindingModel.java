package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.*;
import org.example.gamestoreapp.model.enums.GenreName;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AddGameBindingModel {
    @Size(min = 3, max = 50, message = "Game title must be between 3 and 50 characters!")
    private String title;

    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters!")
    private String description;

    @Pattern(regexp = "^(http(s?):)([/|.\\w\\s:])*\\.(?:jpg|jpeg|png)$", message = "Image URL must be a valid image format!")
    private String imageUrl;

    @Past(message = "Release date must be in the past!")
    private LocalDate releaseDate;

    @NotBlank(message = "Publisher cannot be blank!")
    private String publisher;

    @NotNull(message = "Genre is required!")
    private GenreName genre;

    @Positive(message = "Price must be a positive value!")
    private BigDecimal price;

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

    public GenreName getGenre() {
        return genre;
    }

    public void setGenre(GenreName genre) {
        this.genre = genre;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
