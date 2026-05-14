package com.devbuildv.gestion_chariter.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organisations")
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String adresseLegale;
    private String fiscalId;
    private String contactPrincipal;
    private String logoUrl;

    @Column(length = 2000)
    private String description;

    private boolean validated = false;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private User admin;

    @OneToMany(mappedBy = "organisation")
    private List<ActionCharite> actions = new ArrayList<>();

    // Constructeurs
    public Organisation() {}

    public Organisation(String nom, User admin) {
        this.nom = nom;
        this.admin = admin;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getAdresseLegale() { return adresseLegale; }
    public void setAdresseLegale(String adresseLegale) { this.adresseLegale = adresseLegale; }
    public String getFiscalId() { return fiscalId; }
    public void setFiscalId(String fiscalId) { this.fiscalId = fiscalId; }
    public String getContactPrincipal() { return contactPrincipal; }
    public void setContactPrincipal(String contactPrincipal) { this.contactPrincipal = contactPrincipal; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isValidated() { return validated; }
    public void setValidated(boolean validated) { this.validated = validated; }
    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }
    public List<ActionCharite> getActions() { return actions; }
    public void setActions(List<ActionCharite> actions) { this.actions = actions; }
}
