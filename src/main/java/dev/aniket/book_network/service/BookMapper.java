package dev.aniket.book_network.service;

import dev.aniket.book_network.model.Book;
import dev.aniket.book_network.request.BookRequest;
import dev.aniket.book_network.request.BookResponse;
import org.springframework.stereotype.Service;

@Service
public class BookMapper {

    public Book toBook(BookRequest request) {
        return Book
                .builder()
                .id(request.id())
                .title(request.title())
                .authorName(request.authorName())
                .synopsis(request.synopsis())
                .archived(false)
                .shareable(request.shareable())
                .build();
    }

    public BookResponse toBookResponse(Book book) {
        return BookResponse
                .builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .owner(book.getOwner().getFullName())
                // todo implement this later
//                .cover()
                .rate(book.getRate())
                .archived(book.isArchived())
                .sharable(book.isShareable())
                .build();
    }
}
