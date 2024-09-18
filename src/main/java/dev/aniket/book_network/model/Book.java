package dev.aniket.book_network.model;

import dev.aniket.book_network.common.BaseEntity;
import dev.aniket.book_network.feedBack.FeedBack;
import dev.aniket.book_network.history.BookTransactionHistory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Book extends BaseEntity {
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archived;
    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "book", fetch = FetchType.EAGER)
    private List<FeedBack> feedBacks;

    @OneToMany(mappedBy = "book", fetch = FetchType.EAGER)
    private List<BookTransactionHistory> histories;
}
