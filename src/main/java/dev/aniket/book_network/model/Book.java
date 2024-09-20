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

    @Transient
    public double getRate() {
        if (feedBacks == null || feedBacks.isEmpty()) {
            return 0.0;
        }
        double rate = feedBacks
                .stream()
                .mapToDouble(FeedBack::getNote)
                .average()
                .orElse(0.0);
        return Math.round(rate * 10.0) / 10.0; // rounded the value
        // run this System.out.println(Math.round(5.23 * 10.0));System.out.println(Math.round(5.23 * 10.0) / 10.0);
    }
}
