package com.devbuildv.gestion_chariter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dons")
public class Don {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private ActionCharite action;

    private BigDecimal montant;
    private LocalDateTime dateDon;
    private String paymentMethod;
    private String transactionId;

    // Constructeurs
    public Don() {}

    public Don(User user, ActionCharite action, BigDecimal montant, String paymentMethod) {
        this.user = user;
        this.action = action;
        this.montant = montant;
        this.paymentMethod = paymentMethod;
        this.dateDon = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ActionCharite getAction() { return action; }
    public void setAction(ActionCharite action) { this.action = action; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    public LocalDateTime getDateDon() { return dateDon; }
    public void setDateDon(LocalDateTime dateDon) { this.dateDon = dateDon; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}