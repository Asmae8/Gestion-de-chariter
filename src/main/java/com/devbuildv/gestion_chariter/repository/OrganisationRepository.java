package com.devbuildv.gestion_chariter.repository;

import com.devbuildv.gestion_chariter.model.Organisation;
import com.devbuildv.gestion_chariter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    List<Organisation> findByAdmin(User admin);
    List<Organisation> findByValidatedFalse();
    List<Organisation> findByValidatedTrue();
    boolean existsByNom(String nom);
}
