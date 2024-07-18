package com.edm.edmcore.controller;

import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.model.ScheduleRequest;
import com.edm.edmcore.repository.ScheduleRepository;
import com.edm.edmcore.schedule.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleGenerator scheduleGenerator;
    private final ScheduleRepository scheduleRepository;

    @PostMapping("/api/v1/schedule")
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleRequest scheduleRequest) {
        Schedule schedule = scheduleGenerator.generateSchedule(scheduleRequest);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/api/v1/schedule")
    public ResponseEntity<Set<Schedule>> getSchedule(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        List<LocalDate> period = createPeriods(from, to);

        Set<Schedule> schedules = new LinkedHashSet<>();
        period.forEach(day -> schedules.add(scheduleRepository.findFirstByDay(day)));

        return ResponseEntity.ok(schedules);
    }

    private static List<LocalDate> createPeriods(LocalDate from, LocalDate to) {
        List<LocalDate> dayPeriods = new LinkedList<>();

        dayPeriods.add(from);
        dayPeriods.add(to);

        long interval = DAYS.between(from, to);

        for (int i = 1; i < interval; i++) {
            LocalDate date = from.plusDays(i);
            dayPeriods.add(date);
        }

        dayPeriods.sort(LocalDate::compareTo);

        return dayPeriods;
    }


}

