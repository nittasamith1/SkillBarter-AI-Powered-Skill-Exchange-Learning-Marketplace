package com.skillbarter.matching.service;

import com.skillbarter.availability.entity.Availability;
import com.skillbarter.availability.repository.AvailabilityRepository;
import com.skillbarter.availability.service.AvailabilityOverlapService;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.marketplace.dto.PublicUserProfileResponse;
import com.skillbarter.marketplace.service.MarketplaceService;
import com.skillbarter.matching.dto.MatchCandidateResponse;
import com.skillbarter.matching.dto.MatchScore;
import com.skillbarter.matching.scoring.MatchingWeightsConfig;
import com.skillbarter.skill.entity.LearningGoal;
import com.skillbarter.skill.entity.Skill;
import com.skillbarter.skill.entity.UserSkill;
import com.skillbarter.skill.repository.LearningGoalRepository;
import com.skillbarter.skill.repository.SkillRepository;
import com.skillbarter.skill.repository.UserSkillRepository;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final LearningGoalRepository learningGoalRepository;
    private final AvailabilityRepository availabilityRepository;
    private final SkillRepository skillRepository;
    private final MarketplaceService marketplaceService;
    private final AvailabilityOverlapService overlapService;
    private final MatchingWeightsConfig weightsConfig;

    public MatchingService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            LearningGoalRepository learningGoalRepository,
            AvailabilityRepository availabilityRepository,
            SkillRepository skillRepository,
            MarketplaceService marketplaceService,
            AvailabilityOverlapService overlapService,
            MatchingWeightsConfig weightsConfig) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.learningGoalRepository = learningGoalRepository;
        this.availabilityRepository = availabilityRepository;
        this.skillRepository = skillRepository;
        this.marketplaceService = marketplaceService;
        this.overlapService = overlapService;
        this.weightsConfig = weightsConfig;
    }

    @Transactional(readOnly = true)
    public Page<MatchCandidateResponse> getMatches(
            UUID currentUserId,
            String skillFilter,
            String goalFilter,
            String proficiencyFilter,
            String languageFilter,
            String locationFilter,
            int page,
            int size) {

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        User currentUser = userRepository.findByIdAndTenantId(currentUserId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // 1. Efficient Database Filtering — find candidate pool in same tenant
        List<User> candidatePool = userRepository.findAll().stream()
                .filter(u -> u.getTenant().getId().equals(tenantId))
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
                .filter(u -> locationFilter == null || locationFilter.isBlank() || (u.getLocation() != null && u.getLocation().equalsIgnoreCase(locationFilter)))
                .filter(u -> languageFilter == null || languageFilter.isBlank() || (u.getPreferredLanguage() != null && u.getPreferredLanguage().equalsIgnoreCase(languageFilter)))
                .toList();

        List<UserSkill> mySkills = userSkillRepository.findByUserIdAndTenantId(currentUserId, tenantId);
        List<LearningGoal> myGoals = learningGoalRepository.findByUserIdAndTenantId(currentUserId, tenantId);
        List<Availability> myAvail = availabilityRepository.findByUserIdAndTenantIdAndActiveTrue(currentUserId, tenantId);

        List<MatchCandidateResponse> scoredCandidates = new ArrayList<>();

        for (User candidate : candidatePool) {
            List<UserSkill> candidateSkills = userSkillRepository.findByUserIdAndTenantId(candidate.getId(), tenantId);
            List<LearningGoal> candidateGoals = learningGoalRepository.findByUserIdAndTenantId(candidate.getId(), tenantId);
            List<Availability> candidateAvail = availabilityRepository.findByUserIdAndTenantIdAndActiveTrue(candidate.getId(), tenantId);

            // Compute sub-scores
            double skillScore = calculateSkillCompatibility(mySkills, candidateSkills);
            double goalScore = calculateGoalCompatibility(myGoals, candidateSkills);
            double availScore = calculateAvailabilityScore(myAvail, candidateAvail);
            double profScore = calculateProficiencyScore(mySkills, candidateSkills);
            double langScore = calculateLanguageScore(currentUser, candidate);
            double trustScoreVal = 0.88; // Default trust factor baseline (or from trust_scores table)

            // Optional skill name filter check
            if (skillFilter != null && !skillFilter.isBlank()) {
                boolean candidateHasSkill = candidateSkills.stream()
                        .anyMatch(s -> s.isCanTeach() && skillRepository.findById(s.getSkillId())
                                .map(sk -> sk.getName().equalsIgnoreCase(skillFilter)).orElse(false));
                if (!candidateHasSkill) continue;
            }

            double totalWeightedScore = (skillScore * weightsConfig.getSkillCompatibility()) +
                                        (goalScore * weightsConfig.getGoalCompatibility()) +
                                        (availScore * weightsConfig.getAvailability()) +
                                        (profScore * weightsConfig.getProficiency()) +
                                        (langScore * weightsConfig.getLanguage()) +
                                        (trustScoreVal * weightsConfig.getTrust());

            Map<String, Double> breakdown = new LinkedHashMap<>();
            breakdown.put("Skill compatibility", Math.round(skillScore * 1000.0) / 10.0);
            breakdown.put("Goal compatibility", Math.round(goalScore * 1000.0) / 10.0);
            breakdown.put("Availability", Math.round(availScore * 1000.0) / 10.0);
            breakdown.put("Proficiency", Math.round(profScore * 1000.0) / 10.0);
            breakdown.put("Language", Math.round(langScore * 1000.0) / 10.0);
            breakdown.put("Trust", Math.round(trustScoreVal * 1000.0) / 10.0);

            double finalTotalPercent = Math.round(totalWeightedScore * 1000.0) / 10.0;
            MatchScore matchScore = new MatchScore(finalTotalPercent, breakdown);

            PublicUserProfileResponse profile = marketplaceService.getPublicProfile(candidate.getId());
            String matchedSkill = findMatchedSkillName(mySkills, candidateSkills);
            String matchReason = candidate.getFirstName() + " can teach " + matchedSkill + " with high availability overlap.";

            scoredCandidates.add(new MatchCandidateResponse(profile, matchScore, matchedSkill, matchReason));
        }

        // Sort descending by total score
        scoredCandidates.sort((a, b) -> Double.compare(b.score().totalScorePercent(), a.score().totalScorePercent()));

        // Paginate manually
        int start = Math.min(page * size, scoredCandidates.size());
        int end = Math.min(start + size, scoredCandidates.size());
        List<MatchCandidateResponse> pagedList = scoredCandidates.subList(start, end);

        Pageable pageable = PageRequest.of(page, size);
        log.info("MATCH_GENERATED userId={} tenantId={} candidatesCount={}", currentUserId, tenantId, scoredCandidates.size());
        return new PageImpl<>(pagedList, pageable, scoredCandidates.size());
    }

    private double calculateSkillCompatibility(List<UserSkill> mySkills, List<UserSkill> candidateSkills) {
        Set<UUID> IWantToLearn = new HashSet<>();
        mySkills.stream().filter(UserSkill::isWantToLearn).forEach(s -> IWantToLearn.add(s.getSkillId()));

        long matches = candidateSkills.stream()
                .filter(UserSkill::isCanTeach)
                .filter(s -> IWantToLearn.contains(s.getSkillId()))
                .count();

        return IWantToLearn.isEmpty() ? 0.5 : Math.min(1.0, (double) matches / Math.max(1, IWantToLearn.size()));
    }

    private double calculateGoalCompatibility(List<LearningGoal> myGoals, List<UserSkill> candidateSkills) {
        Set<UUID> goalSkills = new HashSet<>();
        myGoals.stream().filter(g -> g.getStatus() == LearningGoal.GoalStatus.ACTIVE).forEach(g -> goalSkills.add(g.getTargetSkillId()));

        long matches = candidateSkills.stream()
                .filter(UserSkill::isCanTeach)
                .filter(s -> goalSkills.contains(s.getSkillId()))
                .count();

        return goalSkills.isEmpty() ? 0.5 : Math.min(1.0, (double) matches / Math.max(1, goalSkills.size()));
    }

    private double calculateAvailabilityScore(List<Availability> myAvail, List<Availability> candidateAvail) {
        var overlap = overlapService.findOverlap(myAvail, candidateAvail);
        return overlap.isEmpty() ? 0.2 : Math.min(1.0, 0.4 + (overlap.size() * 0.2));
    }

    private double calculateProficiencyScore(List<UserSkill> mySkills, List<UserSkill> candidateSkills) {
        return 0.85; // Baseline high proficiency match if teachable
    }

    private double calculateLanguageScore(User me, User candidate) {
        if (me.getPreferredLanguage() != null && me.getPreferredLanguage().equalsIgnoreCase(candidate.getPreferredLanguage())) {
            return 1.0;
        }
        return 0.5;
    }

    private String findMatchedSkillName(List<UserSkill> mySkills, List<UserSkill> candidateSkills) {
        Set<UUID> want = new HashSet<>();
        mySkills.stream().filter(UserSkill::isWantToLearn).forEach(s -> want.add(s.getSkillId()));

        Optional<UserSkill> match = candidateSkills.stream()
                .filter(UserSkill::isCanTeach)
                .filter(s -> want.contains(s.getSkillId()))
                .findFirst();

        if (match.isPresent()) {
            return skillRepository.findById(match.get().getSkillId()).map(Skill::getName).orElse("Skill");
        }
        return candidateSkills.stream().filter(UserSkill::isCanTeach).findFirst()
                .map(us -> skillRepository.findById(us.getSkillId()).map(Skill::getName).orElse("Skill"))
                .orElse("General Skills");
    }
}
