package org.example.gamestoreapp.model.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class GameDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String videoUrl;
    private String releaseDate;
    private String publisher;
    private String genre;
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

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameDTO gameDTO)) return false;

        return Objects.equals(id, gameDTO.id) &&
                Objects.equals(title, gameDTO.title) &&
                Objects.equals(description, gameDTO.description) &&
                Objects.equals(imageUrl, gameDTO.imageUrl) &&
                Objects.equals(videoUrl, gameDTO.videoUrl) &&
                Objects.equals(releaseDate, gameDTO.releaseDate) &&
                Objects.equals(publisher, gameDTO.publisher) &&
                Objects.equals(genre, gameDTO.genre) &&
                Objects.equals(price, gameDTO.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, imageUrl, videoUrl, releaseDate, publisher, genre, price);
    }
}
