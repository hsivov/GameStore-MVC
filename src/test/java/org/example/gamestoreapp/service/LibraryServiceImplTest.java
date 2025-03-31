package org.example.gamestoreapp.service;

import org.example.gamestoreapp.model.entity.Game;
import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.impl.LibraryServiceImpl;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceImplTest {

    @Mock
    private UserHelperService userHelperService;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Test
    void testIsGameInLibrary_UserDoesExists() {
        Game mockGame = new Game();
        mockGame.setId(1L);
        mockGame.setTitle("Game Title");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setOwnedGames(List.of(mockGame));

        when(userHelperService.getUser()).thenReturn(mockUser);

        boolean result = libraryService.isGameInLibrary(1L);

        assertTrue(result);

        verify(userHelperService, times(1)).getUser();
    }

    @Test
    void testIsGameInLibrary_UserDoesNotExist() {
        when(userHelperService.getUser()).thenReturn(null);

        boolean result = libraryService.isGameInLibrary(1L);

        assertFalse(result);
    }
}