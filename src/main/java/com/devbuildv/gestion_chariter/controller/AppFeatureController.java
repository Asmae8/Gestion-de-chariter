package com.devbuildv.gestion_chariter.controller;

import com.devbuildv.gestion_chariter.model.ActionCharite;
import com.devbuildv.gestion_chariter.model.Don;
import com.devbuildv.gestion_chariter.model.Organisation;
import com.devbuildv.gestion_chariter.model.Participation;
import com.devbuildv.gestion_chariter.model.Role;
import com.devbuildv.gestion_chariter.model.User;
import com.devbuildv.gestion_chariter.repository.ActionChariteRepository;
import com.devbuildv.gestion_chariter.repository.DonRepository;
import com.devbuildv.gestion_chariter.repository.OrganisationRepository;
import com.devbuildv.gestion_chariter.repository.ParticipationRepository;
import com.devbuildv.gestion_chariter.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@Validated
public class AppFeatureController {

    private final ActionChariteRepository actionRepository;
    private final OrganisationRepository organisationRepository;
    private final DonRepository donRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;

    public AppFeatureController(
            ActionChariteRepository actionRepository,
            OrganisationRepository organisationRepository,
            DonRepository donRepository,
            ParticipationRepository participationRepository,
            UserRepository userRepository
    ) {
        this.actionRepository = actionRepository;
        this.organisationRepository = organisationRepository;
        this.donRepository = donRepository;
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(Authentication authentication) {
        User currentUser = getCurrentUserOrNull(authentication);
        Role currentRole = currentUser != null ? currentUser.getRole() : null;
        boolean canApproveOrganisations = currentRole == Role.SUPER_ADMIN;
        boolean canCreateOrganisation = currentRole == Role.ORG_ADMIN || currentRole == Role.SUPER_ADMIN;
        boolean canCreateAction = currentRole == Role.ORG_ADMIN || currentRole == Role.SUPER_ADMIN;
        boolean canContribute = currentRole == Role.USER;

        List<OrganisationItem> organisations = organisationRepository.findAll().stream()
                .map(org -> new OrganisationItem(
                        org.getId(),
                        org.getNom(),
                        org.getDescription(),
                        org.getContactPrincipal(),
                        org.isValidated(),
                        canApproveOrganisations && !org.isValidated(),
                        canApproveOrganisations || (
                                currentUser != null
                                        && org.getAdmin() != null
                                        && org.getAdmin().getId().equals(currentUser.getId())
                        )
                ))
                .toList();

        List<ActionItem> actions = actionRepository.findByArchivedFalseOrderByCreatedAtDesc().stream()
                .map(action -> new ActionItem(
                        action.getId(),
                        action.getTitre(),
                        action.getDescription(),
                        action.getMediaUrl(),
                        action.getCategorie(),
                        action.getLieu(),
                        action.getDateDebut(),
                        action.getDateFin(),
                        action.getObjectifFonds(),
                        action.getSommeActuelle(),
                        action.getOrganisation() != null ? action.getOrganisation().getNom() : "Sans organisation",
                        participationRepository.findByAction(action).size(),
                        donRepository.findByAction(action).size()
                ))
                .toList();

        return new DashboardResponse(
                organisations,
                actions,
                currentRole != null ? currentRole.name() : "ANONYMOUS",
                canApproveOrganisations,
                canCreateOrganisation,
                canCreateAction,
                canContribute
        );
    }

    @PostMapping("/organisations")
    public ResponseEntity<OrganisationItem> createOrganisation(
            @Valid @RequestBody CreateOrganisationRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.ORG_ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul un administrateur d'organisation peut creer une organisation.");
        }
        if (organisationRepository.existsByNom(request.nom().trim())) {
            throw new IllegalArgumentException("Une organisation avec ce nom existe deja.");
        }

        Organisation organisation = new Organisation();
        organisation.setNom(request.nom().trim());
        organisation.setDescription(blankToNull(request.description()));
        organisation.setAdresseLegale(blankToNull(request.adresseLegale()));
        organisation.setFiscalId(blankToNull(request.fiscalId()));
        organisation.setContactPrincipal(blankToNull(request.contactPrincipal()));
        organisation.setLogoUrl(blankToNull(request.logoUrl()));
        organisation.setAdmin(currentUser);
        organisation.setValidated(currentUser.getRole() == Role.SUPER_ADMIN);

        Organisation saved = organisationRepository.save(organisation);
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrganisationItem(
                saved.getId(),
                saved.getNom(),
                saved.getDescription(),
                saved.getContactPrincipal(),
                saved.isValidated(),
                false,
                true
        ));
    }

    @PostMapping("/actions")
    public ResponseEntity<ActionItem> createAction(
            @Valid @RequestBody CreateActionRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.ORG_ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul un administrateur d'organisation peut creer une action.");
        }
        Organisation organisation = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable."));

        boolean canManage = currentUser.getRole() == Role.SUPER_ADMIN
                || (organisation.getAdmin() != null && organisation.getAdmin().getId().equals(currentUser.getId()));
        if (!canManage) {
            throw new IllegalArgumentException("Vous ne pouvez creer des actions que pour votre organisation.");
        }
        if (!organisation.isValidated()) {
            throw new IllegalArgumentException("L'organisation doit etre validee avant de creer une action.");
        }
        validateActionDates(request.dateDebut(), request.dateFin());

        ActionCharite action = new ActionCharite();
        action.setTitre(request.titre().trim());
        action.setDescription(blankToNull(request.description()));
        action.setCategorie(blankToNull(request.categorie()));
        action.setLieu(blankToNull(request.lieu()));
        action.setDateDebut(request.dateDebut());
        action.setDateFin(request.dateFin());
        action.setObjectifFonds(request.objectifFonds());
        action.setSommeActuelle(BigDecimal.ZERO);
        action.setMediaUrl(blankToNull(request.mediaUrl()));
        action.setOrganisation(organisation);
        action.setArchived(false);
        action.setCreatedAt(LocalDateTime.now());

        ActionCharite saved = actionRepository.save(action);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ActionItem(
                saved.getId(),
                saved.getTitre(),
                saved.getDescription(),
                saved.getMediaUrl(),
                saved.getCategorie(),
                saved.getLieu(),
                saved.getDateDebut(),
                saved.getDateFin(),
                saved.getObjectifFonds(),
                saved.getSommeActuelle(),
                organisation.getNom(),
                0,
                0
        ));
    }

    @PostMapping("/organisations/{organisationId}/approve")
    public ResponseEntity<Map<String, String>> approveOrganisation(
            @PathVariable Long organisationId,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Seul le super administrateur peut valider une organisation.");
        }

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable."));

        if (organisation.isValidated()) {
            throw new IllegalArgumentException("Cette organisation est deja validee.");
        }

        organisation.setValidated(true);
        organisationRepository.save(organisation);
        return ResponseEntity.ok(Map.of("message", "Organisation validee avec succes."));
    }

    @PostMapping("/actions/{actionId}/participate")
    public ResponseEntity<Map<String, String>> participate(
            @PathVariable Long actionId,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.USER) {
            throw new IllegalArgumentException("Seul un utilisateur simple peut participer a une action.");
        }
        ActionCharite action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable."));
        ensureActionAvailable(action);

        if (participationRepository.existsByUserAndAction(currentUser, action)) {
            throw new IllegalArgumentException("Vous participez deja a cette action.");
        }

        Participation participation = new Participation(currentUser, action);
        participationRepository.save(participation);
        return ResponseEntity.ok(Map.of("message", "Participation enregistree."));
    }

    @PostMapping("/actions/{actionId}/donate")
    public ResponseEntity<Map<String, String>> donate(
            @PathVariable Long actionId,
            @Valid @RequestBody DonateRequest request,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser.getRole() != Role.USER) {
            throw new IllegalArgumentException("Seul un utilisateur simple peut faire un don.");
        }
        ActionCharite action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action introuvable."));
        ensureActionAvailable(action);

        BigDecimal amount = request.montant();
        Don don = new Don(currentUser, action, amount, request.paymentMethod().trim());
        don.setTransactionId("TX-" + System.currentTimeMillis());
        donRepository.save(don);

        BigDecimal total = action.getSommeActuelle() == null ? BigDecimal.ZERO : action.getSommeActuelle();
        action.setSommeActuelle(total.add(amount));
        actionRepository.save(action);

        return ResponseEntity.ok(Map.of("message", "Don enregistre avec succes."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Donnée invalide.")
                .orElse("Donnée invalide.");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Utilisateur non authentifie.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    }

    private User getCurrentUserOrNull(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateActionDates(LocalDate dateDebut, LocalDate dateFin) {
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit etre posterieure ou egale a la date de debut.");
        }
    }

    private void ensureActionAvailable(ActionCharite action) {
        if (action.isArchived()) {
            throw new IllegalArgumentException("Cette action est archivee.");
        }
        if (action.getOrganisation() == null || !action.getOrganisation().isValidated()) {
            throw new IllegalArgumentException("Cette action appartient a une organisation non validee.");
        }
        if (action.getDateFin() != null && action.getDateFin().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cette action est deja terminee.");
        }
    }

    public record CreateOrganisationRequest(
            @NotBlank(message = "Le nom est obligatoire") String nom,
            String description,
            String adresseLegale,
            String fiscalId,
            String contactPrincipal,
            String logoUrl
    ) {
    }

    public record CreateActionRequest(
            @NotBlank(message = "Le titre est obligatoire") String titre,
            String description,
            String categorie,
            @NotNull(message = "La date de debut est obligatoire") LocalDate dateDebut,
            @NotNull(message = "La date de fin est obligatoire") LocalDate dateFin,
            String lieu,
            @NotNull(message = "L'objectif est obligatoire")
            @DecimalMin(value = "1.0", inclusive = true, message = "L'objectif doit etre positif")
            BigDecimal objectifFonds,
            String mediaUrl,
            @NotNull(message = "L'organisation est obligatoire") Long organisationId
    ) {
    }

    public record DonateRequest(
            @NotNull(message = "Le montant est obligatoire")
            @DecimalMin(value = "1.0", inclusive = true, message = "Le montant doit etre positif")
            BigDecimal montant,
            @NotBlank(message = "Le mode de paiement est obligatoire") String paymentMethod
    ) {
    }

    public record OrganisationItem(
            Long id,
            String nom,
            String description,
            String contactPrincipal,
            boolean validated,
            boolean canBeApproved,
            boolean canManage
    ) {
    }

    public record ActionItem(
            Long id,
            String titre,
            String description,
            String mediaUrl,
            String categorie,
            String lieu,
            LocalDate dateDebut,
            LocalDate dateFin,
            BigDecimal objectifFonds,
            BigDecimal sommeActuelle,
            String organisationNom,
            int participants,
            int dons
    ) {
    }

    public record DashboardResponse(
            List<OrganisationItem> organisations,
            List<ActionItem> actions,
            String role,
            boolean canApproveOrganisations,
            boolean canCreateOrganisation,
            boolean canCreateAction,
            boolean canContribute
    ) {
    }
}
