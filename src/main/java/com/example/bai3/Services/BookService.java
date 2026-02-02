package com.example.bai3.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.bai3.Model.Book;

@Service
public class BookService {
    private List<Book> books = new ArrayList<>(List.of(
            new Book(1L, "To Kill a Mockingbird", "Harper Lee"),
            new Book(2L, "1984", "George Orwell"),
            new Book(3L, "The Great Gatsby", "F. Scott Fitzgerald"),
            new Book(4L, "The Catcher in the Rye", "J.D. Salinger"),
            new Book(5L, "Tôi thấy hoa vàng trên cỏ xanh", "Nguyễn Nhật Ánh")));

    private long nextId = 6L;

    public List<Book> getAllBooks() {
        return books;
    }

    public Optional<Book> getBookById(Long id) {
        return books.stream().filter(book -> book.getId().equals(id)).findFirst();
    }

    public void addBook(Book book) {
        book.setId(nextId++);
        books.add(book);
    }

    public void deleteBook(Long id) {
        books.removeIf(book -> book.getId().equals(id));
    }

    public void updateBook(Book updatedBook) {
        books.stream()
            .filter(book -> book.getId().equals(updatedBook.getId()))
            .findFirst()
            .ifPresent(book -> {
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
        });
    }
}
