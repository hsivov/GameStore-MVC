package org.example.gamestoreapp.model.entity;

import jakarta.persistence.*;
import org.example.gamestoreapp.model.enums.GenreName;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "titles")
public class Game extends BaseEntity{
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private String imageThumbnail;
    @Column(nullable = false)
    private LocalDate releaseDate;
    @Column(nullable = false)
    private String publisher;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GenreName genre;
    @Column(nullable = false)
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

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
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
