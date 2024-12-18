package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.CommentDTO;
import org.example.gamestoreapp.model.dto.PostCommentDTO;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByGame(Long id);

    void postComment(PostCommentDTO postCommentDTO, Long gameId);
}
