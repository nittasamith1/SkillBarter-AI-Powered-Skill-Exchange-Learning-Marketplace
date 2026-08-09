package com.skillbarter.availability.service;

import com.skillbarter.availability.entity.Availability;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityOverlapService {

    public record OverlapSlot(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String timezone
    ) {}

    /**
     * Calculates overlapping availability slots between two users, normalized to User A's timezone (or target timezone).
     */
    public List<OverlapSlot> findOverlap(List<Availability> userAAvailability, List<Availability> userBAvailability) {
        return findOverlap(userAAvailability, userBAvailability, "UTC");
    }

    public List<OverlapSlot> findOverlap(List<Availability> userAAvailability, List<Availability> userBAvailability, String targetTimezoneStr) {
        ZoneId targetZone = ZoneId.of(targetTimezoneStr);
        List<OverlapSlot> overlaps = new ArrayList<>();

        // Convert each availability slot into UTC-normalized weekly minute ranges (or 7-day windows)
        for (Availability slotA : userAAvailability) {
            if (!slotA.isActive()) continue;
            for (Availability slotB : userBAvailability) {
                if (!slotB.isActive()) continue;

                // Simple check if same day or timezone-adjusted day overlap
                List<TimeWindow> windowsA = convertToUtcWindows(slotA);
                List<TimeWindow> windowsB = convertToUtcWindows(slotB);

                for (TimeWindow wa : windowsA) {
                    for (TimeWindow wb : windowsB) {
                        TimeWindow intersection = wa.intersect(wb);
                        if (intersection != null && intersection.durationMinutes() >= 30) {
                            // Convert back to target timezone
                            overlaps.addAll(convertFromUtcToSlots(intersection, targetZone));
                        }
                    }
                }
            }
        }

        return overlaps;
    }

    /**
     * Helper record representing a start and end instant in a reference week.
     * We use a reference week (e.g. 2026-01-05 (Monday) to 2026-01-11 (Sunday)).
     */
    private record TimeWindow(ZonedDateTime start, ZonedDateTime end) {
        public TimeWindow intersect(TimeWindow other) {
            ZonedDateTime maxStart = this.start.isAfter(other.start) ? this.start : other.start;
            ZonedDateTime minEnd = this.end.isBefore(other.end) ? this.end : other.end;
            if (maxStart.isBefore(minEnd)) {
                return new TimeWindow(maxStart, minEnd);
            }
            return null;
        }

        public long durationMinutes() {
            return Duration.between(start, end).toMinutes();
        }
    }

    private static final LocalDate REF_MONDAY = LocalDate.of(2026, 1, 5); // Known Monday

    private List<TimeWindow> convertToUtcWindows(Availability a) {
        ZoneId zone = ZoneId.of(a.getTimezone());
        int daysFromMonday = a.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate slotDate = REF_MONDAY.plusDays(daysFromMonday);

        ZonedDateTime startLocal = ZonedDateTime.of(slotDate, a.getStartTime(), zone);
        ZonedDateTime endLocal = ZonedDateTime.of(slotDate, a.getEndTime(), zone);

        if (endLocal.isBefore(startLocal) || endLocal.equals(startLocal)) {
            endLocal = endLocal.plusDays(1);
        }

        ZonedDateTime startUtc = startLocal.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endUtc = endLocal.withZoneSameInstant(ZoneOffset.UTC);

        return List.of(new TimeWindow(startUtc, endUtc));
    }

    private List<OverlapSlot> convertFromUtcToSlots(TimeWindow utcWindow, ZoneId targetZone) {
        ZonedDateTime startTarget = utcWindow.start().withZoneSameInstant(targetZone);
        ZonedDateTime endTarget = utcWindow.end().withZoneSameInstant(targetZone);

        List<OverlapSlot> result = new ArrayList<>();
        if (startTarget.toLocalDate().equals(endTarget.toLocalDate())) {
            result.add(new OverlapSlot(
                    startTarget.getDayOfWeek(),
                    startTarget.toLocalTime(),
                    endTarget.toLocalTime(),
                    targetZone.getId()
            ));
        } else {
            // Spans midnight boundary
            result.add(new OverlapSlot(
                    startTarget.getDayOfWeek(),
                    startTarget.toLocalTime(),
                    LocalTime.MAX,
                    targetZone.getId()
            ));
            result.add(new OverlapSlot(
                    endTarget.getDayOfWeek(),
                    LocalTime.MIN,
                    endTarget.toLocalTime(),
                    targetZone.getId()
            ));
        }
        return result;
    }
}
