package com.sharkdom.repository.demo;

import com.sharkdom.entity.demo.DemoBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DemoBookRepository extends JpaRepository<DemoBook, Long> {
    @Query(value = "SELECT e \n" +
            "FROM \n" +
            "   DemoBook e \n" +
            "WHERE \n" +
            "   DATE(creationTimestamp) BETWEEN :from AND :to\n")
    List<DemoBook> findAllFromTo(LocalDate from, LocalDate to);
}
