package dev.aniket.book_network.service;

import dev.aniket.book_network.repository.BookRepository;
import dev.aniket.book_network.model.Book;
import dev.aniket.book_network.model.User;
import dev.aniket.book_network.request.BookRequest;
import dev.aniket.book_network.request.BookResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User userPrinciple = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(userPrinciple);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
    }
}

