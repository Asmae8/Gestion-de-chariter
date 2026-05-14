package com.devbuildv.gestion_chariter;

import com.devbuildv.gestion_chariter.model.ActionCharite;
import com.devbuildv.gestion_chariter.model.Organisation;
import com.devbuildv.gestion_chariter.repository.ActionChariteRepository;
import com.devbuildv.gestion_chariter.repository.OrganisationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GestionChariterApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrganisationRepository organisationRepository;

	@Autowired
	private ActionChariteRepository actionChariteRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void loginPageLoads() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Connexion")));
	}

	@Test
	void loginApiReturnsJwtCookie() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"admin@charity.com","password":"admin123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(cookie().exists("auth_token"));
	}

	@Test
	void registerRejectsInvalidPayload() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"bad-email","password":"password123","nom":"Test","prenom":"User","telephone":"+212600000000"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("email")));
	}

	@Test
	void protectedRouteRejectsAnonymousRequest() throws Exception {
		mockMvc.perform(get("/profile/test"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void donateRejectsExpiredAction() throws Exception {
		Organisation organisation = organisationRepository.findAll().stream()
				.findFirst()
				.orElseThrow();

		ActionCharite expiredAction = new ActionCharite();
		expiredAction.setTitre("Action terminee");
		expiredAction.setDescription("Action expiree pour test");
		expiredAction.setCategorie("Urgence");
		expiredAction.setLieu("Casablanca");
		expiredAction.setDateDebut(LocalDate.now().minusDays(10));
		expiredAction.setDateFin(LocalDate.now().minusDays(1));
		expiredAction.setObjectifFonds(new BigDecimal("1000"));
		expiredAction.setSommeActuelle(BigDecimal.ZERO);
		expiredAction.setOrganisation(organisation);
		expiredAction.setArchived(false);
		expiredAction.setCreatedAt(java.time.LocalDateTime.now());
		expiredAction = actionChariteRepository.save(expiredAction);
		assertNotNull(expiredAction.getId());

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@charity.com","password":"user1234"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String authCookie = loginResult.getResponse().getCookie("auth_token").getValue();

		mockMvc.perform(post("/api/app/actions/" + expiredAction.getId() + "/donate")
						.contentType(MediaType.APPLICATION_JSON)
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie))
						.content("""
								{"montant":100,"paymentMethod":"CARTE"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("terminee")));
	}

	@Test
	void superAdminCanApprovePendingOrganisation() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"admin@charity.com","password":"admin123"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String authCookie = loginResult.getResponse().getCookie("auth_token").getValue();

		mockMvc.perform(post("/api/app/organisations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nom":"Association Test Pending","description":"Association en attente","contactPrincipal":"pending@test.org","adresseLegale":"Casablanca"}
								"""))
				.andExpect(status().isUnauthorized());

		Organisation organisation = new Organisation();
		organisation.setNom("Association Pending Approval");
		organisation.setDescription("Association en attente");
		organisation.setContactPrincipal("pending@test.org");
		organisation.setValidated(false);
		organisation = organisationRepository.save(organisation);

		mockMvc.perform(post("/api/app/organisations/" + organisation.getId() + "/approve")
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("validee")));
	}

	@Test
	void simpleUserCannotApproveOrganisation() throws Exception {
		Organisation organisation = new Organisation();
		organisation.setNom("Association User Reject");
		organisation.setDescription("Association en attente");
		organisation.setContactPrincipal("userreject@test.org");
		organisation.setValidated(false);
		organisation = organisationRepository.save(organisation);

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@charity.com","password":"user1234"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String authCookie = loginResult.getResponse().getCookie("auth_token").getValue();

		mockMvc.perform(post("/api/app/organisations/" + organisation.getId() + "/approve")
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("super administrateur")));
	}

	@Test
	void simpleUserCannotCreateOrganisationOrAction() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@charity.com","password":"user1234"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String authCookie = loginResult.getResponse().getCookie("auth_token").getValue();

		mockMvc.perform(post("/api/app/organisations")
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nom":"Association Refusee","description":"Test","contactPrincipal":"refusee@test.org","adresseLegale":"Casablanca"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("administrateur d'organisation")));

		Organisation organisation = organisationRepository.findAll().stream()
				.filter(Organisation::isValidated)
				.findFirst()
				.orElseThrow();

		mockMvc.perform(post("/api/app/actions")
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie))
						.contentType(MediaType.APPLICATION_JSON)
						.content(String.format("""
								{"titre":"Action refusee","description":"Test","categorie":"Education","dateDebut":"%s","dateFin":"%s","lieu":"Casablanca","objectifFonds":1000,"organisationId":%d}
								""", LocalDate.now(), LocalDate.now().plusDays(5), organisation.getId())))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("administrateur d'organisation")));
	}

	@Test
	void orgAdminCanCreateAnotherOrganisation() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"org@charity.com","password":"org12345"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String authCookie = loginResult.getResponse().getCookie("auth_token").getValue();

		mockMvc.perform(post("/api/app/organisations")
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nom":"Association Deuxieme Org Admin","description":"Deuxieme organisation test","contactPrincipal":"org2@test.org","adresseLegale":"Casablanca"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string(containsString("Association Deuxieme Org Admin")));
	}

	@Test
	void orgAdminCannotDonate() throws Exception {
		ActionCharite action = actionChariteRepository.findAll().stream()
				.findFirst()
				.orElseThrow();

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"org@charity.com","password":"org12345"}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String authCookie = loginResult.getResponse().getCookie("auth_token").getValue();

		mockMvc.perform(post("/api/app/actions/" + action.getId() + "/donate")
						.contentType(MediaType.APPLICATION_JSON)
						.cookie(new jakarta.servlet.http.Cookie("auth_token", authCookie))
						.content("""
								{"montant":100,"paymentMethod":"CARTE"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("utilisateur simple")));
	}
}
