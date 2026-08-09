package com.skillbarter.availability.repository;

import com.skillbarter.availability.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    List<Availability> findByUserIdAndTenantIdAndActiveTrue(UUID userId, UUID tenantId);

    List<Availability> findByUserIdAndActiveTrue(UUID userId);

    Optional<Availability> findByIdAndUserIdAndTenantId(UUID id, UUID userId, UUID tenantId);

    List<Availability> findByUserIdAndDayOfWeekAndActiveTrue(UUID userId, DayOfWeek dayOfWeek);
}
