package com.devbuildv.gestion_chariter.repository;

import com.devbuildv.gestion_chariter.model.Participation;
import com.devbuildv.gestion_chariter.model.User;
import com.devbuildv.gestion_chariter.model.ActionCharite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByUser(User user);
    List<Participation> findByAction(ActionCharite action);
    Optional<Participation> findByUserAndAction(User user, ActionCharite action);
    boolean existsByUserAndAction(User user, ActionCharite action);
}