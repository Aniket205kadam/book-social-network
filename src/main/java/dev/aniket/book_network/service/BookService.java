package dev.aniket.book_network.service;

import dev.aniket.book_network.common.PageResponse;
import dev.aniket.book_network.exception.OperationNotPermittedException;
import dev.aniket.book_network.history.BookTransactionHistory;
import dev.aniket.book_network.repository.BookRepository;
import dev.aniket.book_network.model.Book;
import dev.aniket.book_network.model.User;
import dev.aniket.book_network.repository.BookTransactionHistoryRepository;
import dev.aniket.book_network.request.BookRequest;
import dev.aniket.book_network.request.BookResponse;
import dev.aniket.book_network.request.BookSpecification;
import dev.aniket.book_network.request.BorrowedBookResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;

    @Transactional(readOnly = false)
    public Integer save(BookRequest request, Authentication connectedUser) {
        User userPrinciple = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(userPrinciple);
        return bookRepository.save(book).getId();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
    }

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User userPrinciple = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, userPrinciple.getId());
        List<BookResponse> bookResponses = books
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User userPrinciple = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(userPrinciple.getId()), pageable);
        List<BookResponse> bookResponses = books
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User userPrinciple = ((User) connectedUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable, userPrinciple.getId());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks
                .stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User userPrinciple = ((User) connectedUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdBy").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable, userPrinciple.getId());
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks
                .stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    @Transactional(readOnly = false)
    public Integer updateSharableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
        User userPrinciple = ((User) connectedUser);
        // only owner of the book can update the sharable status
        if (!book.getOwner().getId().equals(userPrinciple.getId())) {
            throw new OperationNotPermittedException("You cannot update other books shareable status");
        }
        // update shareable status
        book.setShareable(!book.isShareable());
        return bookRepository.save(book).getId();
    }

    @Transactional(readOnly = false)
    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
        User userPrinciple = ((User) connectedUser);
        // only owner of the book can update the archived status
        if (!book.getOwner().getId().equals(userPrinciple.getId())) {
            throw new OperationNotPermittedException("You cannot update other books archived status");
        }
        // update archived status
        book.setArchived(!book.isArchived());
        return bookRepository.save(book).getId();
    }
}
