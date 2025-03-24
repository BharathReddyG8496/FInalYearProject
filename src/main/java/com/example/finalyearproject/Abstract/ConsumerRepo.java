package com.example.finalyearproject.Abstract;

import com.example.finalyearproject.DataStore.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerRepo extends JpaRepository<Consumer, Integer> {
}
