package com.devbuildv.gestion_chariter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actions_charite")
public class ActionCharite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 2000)
    private String description;

    private String categorie;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private BigDecimal objectifFonds;
    private BigDecimal sommeActuelle = BigDecimal.ZERO;
    private String mediaUrl;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    private boolean archived = false;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "action")
    private List<Don> dons = new ArrayList<>();

    @OneToMany(mappedBy = "action")
    private List<Participation> participations = new ArrayList<>();

    // Constructeurs
    public ActionCharite() {}

    public ActionCharite(String titre, String description, Organisation organisation) {
        this.titre = titre;
        this.description = description;
        this.organisation = organisation;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public BigDecimal getObjectifFonds() { return objectifFonds; }
    public void setObjectifFonds(BigDecimal objectifFonds) { this.objectifFonds = objectifFonds; }
    public BigDecimal getSommeActuelle() { return sommeActuelle; }
    public void setSommeActuelle(BigDecimal sommeActuelle) { this.sommeActuelle = sommeActuelle; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public Organisation getOrganisation() { return organisation; }
    public void setOrganisation(Organisation organisation) { this.organisation = organisation; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Don> getDons() { return dons; }
    public void setDons(List<Don> dons) { this.dons = dons; }
    public List<Participation> getParticipations() { return participations; }
    public void setParticipations(List<Participation> participations) { this.participations = participations; }
}