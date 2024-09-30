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
        User userPrinciple = ((User) connectedUser.getPrincipal());
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
        User userPrinciple = ((User) connectedUser.getPrincipal());
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
        User userPrinciple = ((User) connectedUser.getPrincipal());
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
        User userPrinciple = ((User) connectedUser.getPrincipal());
        // only owner of the book can update the archived status
        if (!book.getOwner().getId().equals(userPrinciple.getId())) {
            throw new OperationNotPermittedException("You cannot update other books archived status");
        }
        // update archived status
        book.setArchived(!book.isArchived());
        return bookRepository.save(book).getId();
    }

    @Transactional(readOnly = false)
    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
        // if the book shareable or not archived that case you can borrow the book
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not sharable");
        }
        User userPrinciple = ((User) connectedUser.getPrincipal());
        // owner of the book cannot borrow the book
        if (book.getOwner().getId().equals(userPrinciple.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, userPrinciple.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(userPrinciple)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
        // if the book shareable and not that means user can't borrow this book
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not sharable");
        }
        User userPrinciple = ((User) connectedUser.getPrincipal());
        // owner of the book cannot borrow the book, that it also not return the book
        if (book.getOwner().getId().equals(userPrinciple.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(book.getId(), userPrinciple.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));
        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
        // if the book shareable and not that means user can't borrow this book
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not sharable");
        }
        User userPrinciple = ((User) connectedUser.getPrincipal());
        // owner of the book cannot borrow the book, that it also not return the book
        if (book.getOwner().getId().equals(userPrinciple.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(book.getId(), userPrinciple.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));
        //update in the database
        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }
}
