package com.skillbarter.availability.service;

import com.skillbarter.availability.dto.AvailabilityResponse;
import com.skillbarter.availability.dto.CreateAvailabilityRequest;
import com.skillbarter.availability.entity.Availability;
import com.skillbarter.availability.repository.AvailabilityRepository;
import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ErrorCodes;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityService.class);

    private final AvailabilityRepository availabilityRepository;
    private final AvailabilityOverlapService overlapService;

    public AvailabilityService(AvailabilityRepository availabilityRepository, AvailabilityOverlapService overlapService) {
        this.availabilityRepository = availabilityRepository;
        this.overlapService = overlapService;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getUserAvailability(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        return availabilityRepository.findByUserIdAndTenantIdAndActiveTrue(userId, tenantId)
                .stream().map(AvailabilityResponse::from).toList();
    }

    @Transactional
    public AvailabilityResponse createAvailability(UUID userId, CreateAvailabilityRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        validateTimeAndZone(request.startTime(), request.endTime(), request.timezone());

        Availability availability = new Availability();
        availability.setUserId(userId);
        availability.setTenantId(tenantId);
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setTimezone(request.timezone() != null && !request.timezone().isBlank() ? request.timezone() : "UTC");
        availability.setActive(true);

        Availability saved = availabilityRepository.save(availability);
        log.info("AVAILABILITY_CREATED userId={} tenantId={} id={}", userId, tenantId, saved.getId());
        return AvailabilityResponse.from(saved);
    }

    @Transactional
    public AvailabilityResponse updateAvailability(UUID userId, UUID id, CreateAvailabilityRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Availability availability = availabilityRepository.findByIdAndUserIdAndTenantId(id, userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.AVAILABILITY_NOT_FOUND, "Availability slot not found"));

        validateTimeAndZone(request.startTime(), request.endTime(), request.timezone());

        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        if (request.timezone() != null && !request.timezone().isBlank()) {
            availability.setTimezone(request.timezone());
        }

        Availability updated = availabilityRepository.save(availability);
        return AvailabilityResponse.from(updated);
    }

    @Transactional
    public void deleteAvailability(UUID userId, UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new ResourceNotFoundException(ErrorCodes.CROSS_TENANT_ACCESS_DENIED, "No tenant context available");

        Availability availability = availabilityRepository.findByIdAndUserIdAndTenantId(id, userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.AVAILABILITY_NOT_FOUND, "Availability slot not found"));

        availability.setActive(false);
        availabilityRepository.save(availability);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityOverlapService.OverlapSlot> getCommonAvailability(UUID userAId, UUID userBId) {
        List<Availability> availA = availabilityRepository.findByUserIdAndActiveTrue(userAId);
        List<Availability> availB = availabilityRepository.findByUserIdAndActiveTrue(userBId);
        return overlapService.findOverlap(availA, availB);
    }

    private void validateTimeAndZone(java.time.LocalTime start, java.time.LocalTime end, String timezone) {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new BusinessException(ErrorCodes.INVALID_AVAILABILITY, "Start time must be before end time");
        }
        if (timezone != null && !timezone.isBlank()) {
            try {
                ZoneId.of(timezone);
            } catch (Exception e) {
                throw new BusinessException(ErrorCodes.INVALID_AVAILABILITY, "Invalid timezone: " + timezone);
            }
        }
    }
}
