package com.devbuildv.gestion_chariter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participations")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private ActionCharite action;

    private LocalDateTime dateInscription;

    // Constructeurs
    public Participation() {}

    public Participation(User user, ActionCharite action) {
        this.user = user;
        this.action = action;
        this.dateInscription = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ActionCharite getAction() { return action; }
    public void setAction(ActionCharite action) { this.action = action; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
}