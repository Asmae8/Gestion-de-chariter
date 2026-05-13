package com.devbuildv.gestion_chariter.repository;

import com.devbuildv.gestion_chariter.model.Don;
import com.devbuildv.gestion_chariter.model.User;
import com.devbuildv.gestion_chariter.model.ActionCharite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonRepository extends JpaRepository<Don, Long> {
    List<Don> findByUser(User user);
    List<Don> findByAction(ActionCharite action);
    List<Don> findByActionOrderByDateDonDesc(ActionCharite action);
}