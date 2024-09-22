package dev.aniket.book_network.request;

import dev.aniket.book_network.model.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
    public static Specification<Book> withOwnerId(Integer ownerId) {
        //toPredicate
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("owner").get("id"), ownerId);
    }
}

//When you return a Predicate in the lambda expression, it automatically creates the Specification
//By returning a Predicate inside the lambda, you don’t need to manually create a Specification object—Spring does that for you!