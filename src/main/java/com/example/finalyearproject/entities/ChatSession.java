package com.example.finalyearproject.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_session")
public class ChatSession {
    @Id
    @Column(name = "id", nullable = false)
    private Integer chatsessionId;


}