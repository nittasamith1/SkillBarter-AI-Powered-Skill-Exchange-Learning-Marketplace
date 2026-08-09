package com.skillbarter.availability.dto;

import com.skillbarter.availability.entity.Availability;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityResponse(
        UUID id,
        UUID userId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String timezone,
        boolean active
) {
    public static AvailabilityResponse from(Availability a) {
        return new AvailabilityResponse(
                a.getId(),
                a.getUserId(),
                a.getDayOfWeek(),
                a.getStartTime(),
                a.getEndTime(),
                a.getTimezone(),
                a.isActive()
        );
    }
}
