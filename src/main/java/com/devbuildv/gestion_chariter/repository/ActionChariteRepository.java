package com.devbuildv.gestion_chariter.repository;

import com.devbuildv.gestion_chariter.model.ActionCharite;
import com.devbuildv.gestion_chariter.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActionChariteRepository extends JpaRepository<ActionCharite, Long> {
    List<ActionCharite> findByOrganisationAndArchivedFalse(Organisation organisation);
    List<ActionCharite> findByArchivedFalse();
    List<ActionCharite> findByCategorie(String categorie);
    List<ActionCharite> findByArchivedFalseOrderByCreatedAtDesc();
    List<ActionCharite> findByOrganisation(Organisation organisation);
}