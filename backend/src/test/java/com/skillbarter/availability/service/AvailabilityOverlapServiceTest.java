package com.skillbarter.availability.service;

import com.skillbarter.availability.entity.Availability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityOverlapServiceTest {

    private final AvailabilityOverlapService overlapService = new AvailabilityOverlapService();

    @Test
    @DisplayName("Should find overlapping time slot when Student A (18:00-20:00) and Student B (19:00-21:00) overlap on Monday")
    void testFindOverlapSameTimezone() {
        Availability slotA = new Availability();
        slotA.setDayOfWeek(DayOfWeek.MONDAY);
        slotA.setStartTime(LocalTime.of(18, 0));
        slotA.setEndTime(LocalTime.of(20, 0));
        slotA.setTimezone("UTC");
        slotA.setActive(true);

        Availability slotB = new Availability();
        slotB.setDayOfWeek(DayOfWeek.MONDAY);
        slotB.setStartTime(LocalTime.of(19, 0));
        slotB.setEndTime(LocalTime.of(21, 0));
        slotB.setTimezone("UTC");
        slotB.setActive(true);

        var overlaps = overlapService.findOverlap(List.of(slotA), List.of(slotB), "UTC");

        assertFalse(overlaps.isEmpty());
        assertEquals(1, overlaps.size());
        assertEquals(DayOfWeek.MONDAY, overlaps.get(0).dayOfWeek());
        assertEquals(LocalTime.of(19, 0), overlaps.get(0).startTime());
        assertEquals(LocalTime.of(20, 0), overlaps.get(0).endTime());
    }
}
