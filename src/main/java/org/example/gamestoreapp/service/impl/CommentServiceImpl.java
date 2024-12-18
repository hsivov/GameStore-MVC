package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.dto.CommentDTO;
import org.example.gamestoreapp.model.dto.PostCommentDTO;
import org.example.gamestoreapp.model.entity.Comment;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;

import org.example.gamestoreapp.repository.CommentRepository;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.service.CommentService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final GameRepository gameRepository;
    private final UserHelperService userHelperService;

    public CommentServiceImpl(CommentRepository commentRepository, GameRepository gameRepository, UserHelperService userHelperService) {
        this.commentRepository = commentRepository;
        this.gameRepository = gameRepository;
        this.userHelperService = userHelperService;
    }

    @Override
    public List<CommentDTO> getCommentsByGame(Long id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm");

        return commentRepository.findByGame_Id(id).stream()
                .map(comment -> {
                    CommentDTO commentDTO = new CommentDTO();
                    commentDTO.setContent(comment.getContent());
                    commentDTO.setAuthor(comment.getAuthor().getUsername());
                    commentDTO.setCreatedAt(formatter.format(comment.getCreatedAt()));
                    return commentDTO;
                })
                .toList();
    }

    @Override
    public void postComment(PostCommentDTO postCommentDTO, Long gameId) {
        Optional<Game> byId = gameRepository.findById(gameId);
        User currentUser = userHelperService.getUser();

        if (byId.isPresent()) {
            Comment comment = new Comment();
            comment.setContent(postCommentDTO.getComment());
            comment.setAuthor(currentUser);
            comment.setGame(byId.get());
            comment.setCreatedAt(LocalDateTime.now());

            commentRepository.save(comment);
        }
    }
}
