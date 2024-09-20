package dev.aniket.book_network.request;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Integer id;
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String owner;
    private byte[] cover; //picture of the book
    private double rate;
    private boolean archived;
    private boolean sharable;
}
