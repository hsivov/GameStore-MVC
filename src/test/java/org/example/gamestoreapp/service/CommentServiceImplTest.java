package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.dto.CommentDTO;
import org.example.gamestoreapp.model.dto.PostCommentDTO;
import org.example.gamestoreapp.model.entity.Comment;
import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.repository.CommentRepository;
import org.example.gamestoreapp.repository.GameRepository;
import org.example.gamestoreapp.service.impl.CommentServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserHelperService userHelperService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;
    private User mockUser;
    private Game mockGame;
    private final Long gameId = 1L;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setProfileImageUrl("https://example.com/avatar.jpg");

        mockGame = new Game();
        mockGame.setId(gameId);

        comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment!");
        comment.setAuthor(mockUser);
        comment.setCreatedAt(LocalDateTime.of(2024,11,22, 12,52));
    }

    @Test
    void testGetCommentsByGame() {
        List<Comment> mockComments = List.of(comment);

        when(commentRepository.findByGame_Id(gameId)).thenReturn(mockComments);

        List<CommentDTO> result = commentService.getCommentsByGame(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test comment!", result.get(0).getContent());
        assertEquals("testUser", result.get(0).getAuthorName());
        assertEquals("https://example.com/avatar.jpg", result.get(0).getAuthorAvatar());
        assertEquals("22.11.2024 12:52", result.get(0).getCreatedAt());

        verify(commentRepository, times(1)).findByGame_Id(1L);
    }

    @Test
    void testPostComment_Success() {
        PostCommentDTO mockPostCommentDTO = new PostCommentDTO();
        mockPostCommentDTO.setComment("Test comment!");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(userHelperService.getUser()).thenReturn(mockUser);

        commentService.postComment(mockPostCommentDTO, gameId);

        verify(gameRepository, times(1)).findById(gameId);
        verify(userHelperService, times(1)).getUser();
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testPostComment_GameNotFound() {
        PostCommentDTO mockPostCommentDTO = new PostCommentDTO();
        mockPostCommentDTO.setComment("Test comment!");

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        commentService.postComment(mockPostCommentDTO, gameId);

        verify(gameRepository, times(1)).findById(gameId);
        verify(commentRepository, never()).save(any(Comment.class));
    }
}
