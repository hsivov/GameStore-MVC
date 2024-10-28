package org.example.gamestoreapp.model.dto;

import jakarta.validation.constraints.NotBlank;

public class PostCommentDTO {
    @NotBlank
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
