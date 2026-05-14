package com.devbuildv.gestion_chariter.controller;

import com.devbuildv.gestion_chariter.model.Role;
import com.devbuildv.gestion_chariter.model.User;
import com.devbuildv.gestion_chariter.model.Organisation;
import com.devbuildv.gestion_chariter.model.ActionCharite;
import com.devbuildv.gestion_chariter.repository.ActionChariteRepository;
import com.devbuildv.gestion_chariter.repository.OrganisationRepository;
import com.devbuildv.gestion_chariter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class HomeController implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ActionChariteRepository actionChariteRepository;

    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Bienvenue sur l'application de Gestion des Actions de Charite");
        return "home";
    }

    @GetMapping("/home")
    public String homePage() {
        return "redirect:/";
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData) {
            return;
        }

        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@charity.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setRole(Role.SUPER_ADMIN);
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);

            User user = new User();
            user.setEmail("user@charity.com");
            user.setPassword(passwordEncoder.encode("user1234"));
            user.setNom("User");
            user.setPrenom("Simple");
            user.setRole(Role.USER);
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            User orgAdmin = new User();
            orgAdmin.setEmail("org@charity.com");
            orgAdmin.setPassword(passwordEncoder.encode("org12345"));
            orgAdmin.setNom("Association");
            orgAdmin.setPrenom("Admin");
            orgAdmin.setRole(Role.ORG_ADMIN);
            orgAdmin.setEnabled(true);
            orgAdmin.setCreatedAt(LocalDateTime.now());
            userRepository.save(orgAdmin);

            System.out.println("\nUTILISATEURS CREES :");
            System.out.println("----------------------------------");
            System.out.println("SUPER_ADMIN : admin@charity.com / admin123");
            System.out.println("ORG_ADMIN   : org@charity.com / org12345");
            System.out.println("USER        : user@charity.com / user1234");
            System.out.println("----------------------------------\n");
        }

        if (organisationRepository.count() == 0) {
            User admin = userRepository.findByEmail("org@charity.com").orElse(null);
            if (admin != null) {
                Organisation organisation = new Organisation();
                organisation.setNom("Association Espoir");
                organisation.setDescription("Organisation de soutien social et humanitaire.");
                organisation.setContactPrincipal("contact@espoir.org");
                organisation.setAdmin(admin);
                organisation.setValidated(true);
                Organisation savedOrg = organisationRepository.save(organisation);

                ActionCharite action = new ActionCharite();
                action.setTitre("Collecte de fournitures scolaires");
                action.setDescription("Collecte de cartables, cahiers et fournitures pour des eleves en difficultes.");
                action.setCategorie("Education");
                action.setLieu("Casablanca");
                action.setDateDebut(LocalDate.now().minusDays(2));
                action.setDateFin(LocalDate.now().plusDays(30));
                action.setObjectifFonds(new BigDecimal("15000"));
                action.setSommeActuelle(new BigDecimal("2500"));
                action.setOrganisation(savedOrg);
                action.setArchived(false);
                action.setCreatedAt(LocalDateTime.now());
                actionChariteRepository.save(action);

                Organisation pendingOrganisation = new Organisation();
                pendingOrganisation.setNom("Association Solidarite En Attente");
                pendingOrganisation.setDescription("Organisation exemple creee pour tester la validation par le super administrateur.");
                pendingOrganisation.setContactPrincipal("validation@solidarite.org");
                pendingOrganisation.setValidated(false);
                organisationRepository.save(pendingOrganisation);
            }
        }
    }
}
