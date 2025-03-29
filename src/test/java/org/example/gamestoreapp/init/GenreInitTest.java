package org.example.gamestoreapp.init;

import org.example.gamestoreapp.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreInitTest {
    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreInit genreInit;

    @Test
    void testRun_WithEmptyGenreRepository() {
        when(genreRepository.count()).thenReturn(0L);

        genreInit.run();

        verify(genreRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testRun_WhenGenresExists() {
        when(genreRepository.count()).thenReturn(7L);

        genreInit.run();

        verify(genreRepository, never()).saveAll(anyList());
    }
}