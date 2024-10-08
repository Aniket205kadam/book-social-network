package dev.aniket.book_network.repository;

import dev.aniket.book_network.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    // todo -> use specification
    @Query("""
            SELECT book 
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.id !=  :userId
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, @Param("userId") Integer userId);

}