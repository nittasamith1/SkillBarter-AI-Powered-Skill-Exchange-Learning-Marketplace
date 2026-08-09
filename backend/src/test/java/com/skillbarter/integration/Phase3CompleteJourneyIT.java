package com.skillbarter.integration;

import com.skillbarter.availability.dto.CreateAvailabilityRequest;
import com.skillbarter.availability.service.AvailabilityService;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.credits.dto.WalletResponse;
import com.skillbarter.credits.service.CreditService;
import com.skillbarter.marketplace.dto.CreateExchangeRequest;
import com.skillbarter.marketplace.dto.ExchangeRequestResponse;
import com.skillbarter.marketplace.dto.RespondExchangeRequest;
import com.skillbarter.marketplace.entity.ExchangeRequest;
import com.skillbarter.marketplace.service.MarketplaceService;
import com.skillbarter.matching.service.MatchingService;
import com.skillbarter.notification.service.NotificationService;
import com.skillbarter.reputation.dto.CreateReviewRequest;
import com.skillbarter.reputation.entity.TrustScore;
import com.skillbarter.reputation.service.ReviewService;
import com.skillbarter.reputation.service.TrustScoreService;
import com.skillbarter.session.dto.CreateSessionRequest;
import com.skillbarter.session.dto.SessionResponse;
import com.skillbarter.session.entity.Session.SessionStatus;
import com.skillbarter.session.service.SessionService;
import com.skillbarter.skill.dto.AddUserSkillRequest;
import com.skillbarter.skill.dto.CreateLearningGoalRequest;
import com.skillbarter.skill.entity.UserSkill;
import com.skillbarter.skill.service.LearningGoalService;
import com.skillbarter.skill.service.UserSkillService;
import com.skillbarter.tenant.entity.Tenant;
import com.skillbarter.tenant.repository.TenantRepository;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Phase3CompleteJourneyIT {

    @Autowired private TenantRepository tenantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserSkillService userSkillService;
    @Autowired private LearningGoalService learningGoalService;
    @Autowired private AvailabilityService availabilityService;
    @Autowired private MatchingService matchingService;
    @Autowired private MarketplaceService marketplaceService;
    @Autowired private SessionService sessionService;
    @Autowired private CreditService creditService;
    @Autowired private ReviewService reviewService;
    @Autowired private TrustScoreService trustScoreService;
    @Autowired private NotificationService notificationService;
    @Autowired private com.skillbarter.skill.repository.SkillRepository skillRepository;
    @Autowired private com.skillbarter.skill.repository.SkillCategoryRepository categoryRepository;

    private Tenant tenantA;
    private Tenant tenantB;
    private User studentA; // Teaches Java, wants Spring Boot
    private User studentB; // Teaches Spring Boot, wants Java
    private User studentB2; // Belongs to Tenant B (for isolation test)

    private UUID javaSkillId;
    private UUID springBootSkillId;

    @BeforeEach
    void setUp() {
        // Create Skill Category & Skills
        com.skillbarter.skill.entity.SkillCategory cat = new com.skillbarter.skill.entity.SkillCategory();
        cat.setName("Programming Test " + UUID.randomUUID().toString().substring(0, 8));
        cat = categoryRepository.save(cat);

        com.skillbarter.skill.entity.Skill javaSkill = new com.skillbarter.skill.entity.Skill();
        javaSkill.setName("Java OOP");
        javaSkill.setCategoryId(cat.getId());
        javaSkill = skillRepository.save(javaSkill);
        javaSkillId = javaSkill.getId();

        com.skillbarter.skill.entity.Skill springSkill = new com.skillbarter.skill.entity.Skill();
        springSkill.setName("Spring Boot");
        springSkill.setCategoryId(cat.getId());
        springSkill = skillRepository.save(springSkill);
        springBootSkillId = springSkill.getId();
        // 1. Create Tenant A & Tenant B
        tenantA = new Tenant();
        tenantA.setName("University Alpha");
        tenantA.setSlug("univ-alpha-" + UUID.randomUUID().toString().substring(0, 8));
        tenantA = tenantRepository.save(tenantA);

        tenantB = new Tenant();
        tenantB.setName("University Beta");
        tenantB.setSlug("univ-beta-" + UUID.randomUUID().toString().substring(0, 8));
        tenantB = tenantRepository.save(tenantB);

        // 2. Create Users
        studentA = new User();
        studentA.setTenant(tenantA);
        studentA.setEmail("studenta-" + UUID.randomUUID() + "@alpha.edu");
        studentA.setPasswordHash("hashed_pwd");
        studentA.setFirstName("Alice");
        studentA.setLastName("Student");
        studentA = userRepository.save(studentA);

        studentB = new User();
        studentB.setTenant(tenantA);
        studentB.setEmail("studentb-" + UUID.randomUUID() + "@alpha.edu");
        studentB.setPasswordHash("hashed_pwd");
        studentB.setFirstName("Bob");
        studentB.setLastName("Student");
        studentB = userRepository.save(studentB);

        studentB2 = new User();
        studentB2.setTenant(tenantB);
        studentB2.setEmail("studentb2-" + UUID.randomUUID() + "@beta.edu");
        studentB2.setPasswordHash("hashed_pwd");
        studentB2.setFirstName("Charlie");
        studentB2.setLastName("TenantB");
        studentB2 = userRepository.save(studentB2);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("COMPLETE END-TO-END JOURNEY: Phase 1 (Auth/Tenant) -> Phase 2 (Skills/Exchange) -> Phase 3 (Match, Schedule, Complete, Settle Credits, Review, Trust Score, Notifications, Tenant Isolation)")
    void testCompleteSkillBarterJourney() {
        // ── STEP 1: Set Tenant A Context ──
        TenantContext.setCurrentTenant(tenantA.getId());

        // ── STEP 2: Configure Skills & Learning Goals ──
        // Alice (Student A) can teach Java OOP, wants to learn Spring Boot
        userSkillService.addUserSkill(studentA.getId(), new AddUserSkillRequest(javaSkillId, UserSkill.SkillLevel.ADVANCED, true, false, 3));
        userSkillService.addUserSkill(studentA.getId(), new AddUserSkillRequest(springBootSkillId, UserSkill.SkillLevel.BEGINNER, false, true, 0));
        learningGoalService.createGoal(studentA.getId(), new CreateLearningGoalRequest(springBootSkillId, "Master Spring Boot microservices", UserSkill.SkillLevel.BEGINNER, UserSkill.SkillLevel.ADVANCED, null, "Hands-on"));

        // Bob (Student B) can teach Spring Boot, wants to learn Java OOP
        userSkillService.addUserSkill(studentB.getId(), new AddUserSkillRequest(springBootSkillId, UserSkill.SkillLevel.EXPERT, true, false, 4));
        userSkillService.addUserSkill(studentB.getId(), new AddUserSkillRequest(javaSkillId, UserSkill.SkillLevel.INTERMEDIATE, false, true, 1));

        // ── STEP 3: Configure Availability ──
        availabilityService.createAvailability(studentA.getId(), new CreateAvailabilityRequest(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), "UTC"));
        availabilityService.createAvailability(studentB.getId(), new CreateAvailabilityRequest(DayOfWeek.MONDAY, LocalTime.of(19, 0), LocalTime.of(22, 0), "UTC"));

        // Verify common availability overlap (19:00-21:00)
        var commonSlots = availabilityService.getCommonAvailability(studentA.getId(), studentB.getId());
        assertFalse(commonSlots.isEmpty());
        assertEquals(DayOfWeek.MONDAY, commonSlots.get(0).dayOfWeek());

        // ── STEP 4: Matching Engine Search ──
        var matchPage = matchingService.getMatches(studentA.getId(), null, null, null, null, null, 0, 10);
        assertFalse(matchPage.getContent().isEmpty());
        assertEquals(studentB.getId(), matchPage.getContent().get(0).candidateProfile().user().id());
        assertTrue(matchPage.getContent().get(0).score().totalScorePercent() > 50.0);

        // ── STEP 5: Create & Accept Exchange Request ──
        ExchangeRequestResponse exchangeReq = marketplaceService.sendExchangeRequest(studentA.getId(),
                new CreateExchangeRequest(studentB.getId(), javaSkillId, springBootSkillId, "Let's barter Java for Spring Boot!"));
        assertEquals(ExchangeRequest.ExchangeStatus.PENDING, exchangeReq.status());

        // Student B accepts request
        ExchangeRequestResponse acceptedReq = marketplaceService.respondToExchangeRequest(studentB.getId(), exchangeReq.id(),
                new RespondExchangeRequest(ExchangeRequest.ExchangeStatus.ACCEPTED));
        assertEquals(ExchangeRequest.ExchangeStatus.ACCEPTED, acceptedReq.status());

        // ── STEP 6: Schedule Session ──
        Instant futureStart = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        Instant futureEnd = futureStart.plus(1, ChronoUnit.HOURS);

        SessionResponse session = sessionService.createSession(studentA.getId(),
                new CreateSessionRequest(acceptedReq.id(), futureStart, futureEnd, "UTC", "https://meet.skillbarter.ai/test-room"));

        assertEquals(SessionStatus.SCHEDULED, session.status());
        assertFalse(session.creditsSettled());

        // ── STEP 7: Session Lifecycle (Start -> Complete) ──
        SessionResponse inProgress = sessionService.startSession(studentB.getId(), session.id());
        assertEquals(SessionStatus.IN_PROGRESS, inProgress.status());

        SessionResponse completed = sessionService.completeSession(studentB.getId(), session.id());
        assertEquals(SessionStatus.COMPLETED, completed.status());
        assertTrue(completed.creditsSettled());

        // ── STEP 8: Verify Credit Settlement ──
        WalletResponse teacherWallet = creditService.getWalletResponse(studentB.getId()); // Bob taught
        WalletResponse learnerWallet = creditService.getWalletResponse(studentA.getId()); // Alice learned

        assertEquals(new BigDecimal("11.00"), teacherWallet.balance()); // +1 credit
        assertEquals(new BigDecimal("9.00"), learnerWallet.balance());   // -1 credit

        // ── STEP 9: Submit Review & Update Trust Score ──
        var review = reviewService.createReview(studentA.getId(), session.id(), new CreateReviewRequest(5, "Excellent Spring Boot explanation!"));
        assertEquals(5, review.rating());

        TrustScore trustScoreB = trustScoreService.getTrustScore(studentB.getId());
        assertNotNull(trustScoreB);
        assertTrue(trustScoreB.getScore().compareTo(BigDecimal.ZERO) > 0);

        // ── STEP 10: Verify Notifications Delivered ──
        var notificationsA = notificationService.getUserNotifications(studentA.getId());
        assertFalse(notificationsA.isEmpty());

        // ── STEP 11: Cross-Tenant Isolation Enforcement ──
        TenantContext.setCurrentTenant(tenantB.getId());
        assertThrows(Exception.class, () -> sessionService.getSessionById(studentB2.getId(), session.id()));
    }
}
