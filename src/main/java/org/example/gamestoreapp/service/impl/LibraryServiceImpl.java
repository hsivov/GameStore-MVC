package org.example.gamestoreapp.service.impl;

import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.service.LibraryService;
import org.example.gamestoreapp.service.session.UserHelperService;
import org.springframework.stereotype.Service;

@Service
public class LibraryServiceImpl implements LibraryService {
    private final UserHelperService userHelperService;

    public LibraryServiceImpl(UserHelperService userHelperService) {
        this.userHelperService = userHelperService;
    }

    @Override
    public boolean isGameInLibrary(Long gameId) {

        User currentUser = userHelperService.getUser();

        if (currentUser == null) {
            return false;
        }

        return currentUser.getOwnedGames().stream()
                .anyMatch(game -> game.getId().equals(gameId));
    }
}
