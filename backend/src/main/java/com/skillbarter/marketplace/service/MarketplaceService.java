package com.skillbarter.marketplace.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.marketplace.dto.*;
import com.skillbarter.marketplace.entity.ExchangeRequest;
import com.skillbarter.marketplace.repository.ExchangeRequestRepository;
import com.skillbarter.skill.dto.LearningGoalResponse;
import com.skillbarter.skill.dto.UserSkillResponse;
import com.skillbarter.skill.entity.LearningGoal;
import com.skillbarter.skill.entity.Skill;
import com.skillbarter.skill.entity.UserSkill;
import com.skillbarter.skill.repository.LearningGoalRepository;
import com.skillbarter.skill.repository.SkillRepository;
import com.skillbarter.skill.repository.UserSkillRepository;
import com.skillbarter.skill.service.LearningGoalService;
import com.skillbarter.skill.service.UserSkillService;
import com.skillbarter.user.dto.UserResponse;
import com.skillbarter.user.entity.User;
import com.skillbarter.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketplaceService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final LearningGoalRepository learningGoalRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final SkillRepository skillRepository;
    private final UserSkillService userSkillService;
    private final LearningGoalService learningGoalService;

    public MarketplaceService(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            LearningGoalRepository learningGoalRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            SkillRepository skillRepository,
            UserSkillService userSkillService,
            LearningGoalService learningGoalService) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.learningGoalRepository = learningGoalRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.skillRepository = skillRepository;
        this.userSkillService = userSkillService;
        this.learningGoalService = learningGoalService;
    }

    @Transactional(readOnly = true)
    public PublicUserProfileResponse getPublicProfile(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException("No tenant context available");

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<UserSkillResponse> allSkills = userSkillService.getUserSkills(userId);
        List<UserSkillResponse> teaching = allSkills.stream().filter(UserSkillResponse::canTeach).toList();
        List<UserSkillResponse> learning = allSkills.stream().filter(UserSkillResponse::wantToLearn).toList();
        List<LearningGoalResponse> goals = learningGoalService.getUserGoals(userId);

        return new PublicUserProfileResponse(UserResponse.from(user), teaching, learning, goals);
    }

    @Transactional(readOnly = true)
    public List<PublicUserProfileResponse> searchUsers(String query, UUID skillId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException("No tenant context available");

        Set<UUID> matchingUserIds = new HashSet<>();

        if (skillId != null) {
            List<UserSkill> skills = userSkillRepository.findTeachersInTenant(tenantId, skillId);
            skills.forEach(us -> matchingUserIds.add(us.getUserId()));
        } else if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            List<User> users = userRepository.findAll().stream()
                    .filter(u -> u.getTenant().getId().equals(tenantId))
                    .filter(u -> u.getFirstName().toLowerCase().contains(q) ||
                                 u.getLastName().toLowerCase().contains(q) ||
                                 u.getEmail().toLowerCase().contains(q))
                    .toList();
            users.forEach(u -> matchingUserIds.add(u.getId()));
        } else {
            List<User> users = userRepository.findAll().stream()
                    .filter(u -> u.getTenant().getId().equals(tenantId))
                    .limit(20)
                    .toList();
            users.forEach(u -> matchingUserIds.add(u.getId()));
        }

        return matchingUserIds.stream()
                .map(this::getPublicProfile)
                .toList();
    }

    @Transactional
    public ExchangeRequestResponse sendExchangeRequest(UUID requesterId, CreateExchangeRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException("No tenant context available");

        if (requesterId.equals(request.receiverId())) {
            throw new BusinessException("Cannot send exchange request to yourself");
        }

        userRepository.findByIdAndTenantId(request.receiverId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found in tenant"));

        skillRepository.findById(request.offeredSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Offered skill not found"));

        skillRepository.findById(request.wantedSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Wanted skill not found"));

        if (exchangeRequestRepository.existsByRequesterIdAndReceiverIdAndOfferedSkillIdAndWantedSkillIdAndStatus(
                requesterId, request.receiverId(), request.offeredSkillId(), request.wantedSkillId(), ExchangeRequest.ExchangeStatus.PENDING)) {
            throw new BusinessException("A pending exchange request already exists for these skills");
        }

        ExchangeRequest er = new ExchangeRequest();
        er.setTenantId(tenantId);
        er.setRequesterId(requesterId);
        er.setReceiverId(request.receiverId());
        er.setOfferedSkillId(request.offeredSkillId());
        er.setWantedSkillId(request.wantedSkillId());
        er.setMessage(request.message());
        er.setStatus(ExchangeRequest.ExchangeStatus.PENDING);

        ExchangeRequest saved = exchangeRequestRepository.save(er);
        return mapToExchangeResponse(saved);
    }

    @Transactional
    public ExchangeRequestResponse respondToExchangeRequest(UUID userId, UUID requestId, RespondExchangeRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException("No tenant context available");

        ExchangeRequest er = exchangeRequestRepository.findByIdAndTenantId(requestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange request not found"));

        if (!er.getReceiverId().equals(userId)) {
            throw new BusinessException("Only the receiver can respond to this exchange request");
        }

        if (er.getStatus() != ExchangeRequest.ExchangeStatus.PENDING) {
            throw new BusinessException("Exchange request is no longer pending");
        }

        er.setStatus(request.status());
        ExchangeRequest updated = exchangeRequestRepository.save(er);
        return mapToExchangeResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequestResponse> getMyExchangeRequests(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException("No tenant context available");

        List<ExchangeRequest> requests = exchangeRequestRepository.findAllUserRequestsInTenant(userId, tenantId);
        return requests.stream().map(this::mapToExchangeResponse).toList();
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException("No tenant context available");

        List<UserSkillResponse> mySkills = userSkillService.getUserSkills(userId);
        List<LearningGoalResponse> myGoals = learningGoalService.getUserGoals(userId);

        List<ExchangeRequestResponse> pendingRequests = getMyExchangeRequests(userId).stream()
                .filter(r -> r.status() == ExchangeRequest.ExchangeStatus.PENDING && r.receiverId().equals(userId))
                .toList();

        // Calculate recommendations: find other users in same tenant who can teach what I want to learn
        List<RecommendedMatch> recommendations = new ArrayList<>();
        List<UUID> wantedSkillIds = mySkills.stream()
                .filter(UserSkillResponse::wantToLearn)
                .map(UserSkillResponse::skillId)
                .toList();

        List<UUID> goalSkillIds = myGoals.stream()
                .filter(g -> g.status() == LearningGoal.GoalStatus.ACTIVE)
                .map(LearningGoalResponse::targetSkillId)
                .toList();

        Set<UUID> targetSkillIds = new HashSet<>();
        targetSkillIds.addAll(wantedSkillIds);
        targetSkillIds.addAll(goalSkillIds);

        for (UUID skillId : targetSkillIds) {
            List<UserSkill> teachers = userSkillRepository.findTeachersInTenant(tenantId, skillId);
            for (UserSkill teacher : teachers) {
                if (!teacher.getUserId().equals(userId)) {
                    PublicUserProfileResponse profile = getPublicProfile(teacher.getUserId());
                    String skillName = skillRepository.findById(skillId).map(Skill::getName).orElse("Skill");
                    recommendations.add(new RecommendedMatch(
                            profile,
                            skillName,
                            profile.user().firstName() + " can teach " + skillName + " which matches your learning goals"
                    ));
                    if (recommendations.size() >= 5) break;
                }
            }
            if (recommendations.size() >= 5) break;
        }

        return new DashboardSummaryResponse(mySkills, myGoals, recommendations, pendingRequests);
    }

    private ExchangeRequestResponse mapToExchangeResponse(ExchangeRequest er) {
        String requesterName = userRepository.findById(er.getRequesterId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");
        String receiverName = userRepository.findById(er.getReceiverId())
                .map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown");
        String offeredSkillName = skillRepository.findById(er.getOfferedSkillId())
                .map(Skill::getName).orElse("Unknown");
        String wantedSkillName = skillRepository.findById(er.getWantedSkillId())
                .map(Skill::getName).orElse("Unknown");

        return new ExchangeRequestResponse(
                er.getId(),
                er.getRequesterId(),
                requesterName,
                er.getReceiverId(),
                receiverName,
                er.getOfferedSkillId(),
                offeredSkillName,
                er.getWantedSkillId(),
                wantedSkillName,
                er.getMessage(),
                er.getStatus(),
                er.getCreatedAt()
        );
    }
}
