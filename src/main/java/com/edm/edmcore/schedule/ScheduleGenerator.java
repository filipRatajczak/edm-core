package com.edm.edmcore.schedule;

import com.edm.edmcore.client.DispositionClient;
import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleGenerator {
    private final DispositionClient dispositionClient;

    private final ScheduleUtil scheduleUtil;
    private final ScheduleRepository scheduleRepository;


    public List<Schedule> generateSchedule(LocalDate from, LocalDate to) {

        List<Schedule> schedules = new ArrayList<>();

        List<LocalDate> dateObjectsBetween = createDateObjectsBetween(from, to);

        for (LocalDate currentDate : dateObjectsBetween) {
            if (scheduleRepository.findByDay(currentDate).isEmpty()) {
                scheduleRepository.deleteAllByDay(currentDate);
            }
            LinkedHashSet<DispositionDto> currentDateDispositions = new LinkedHashSet<>(dispositionClient.getAllDisposition(currentDate, currentDate));
            Schedule scheduledDisposition = scheduleUtil.generateSchedule(currentDateDispositions, currentDate);
            schedules.add(scheduledDisposition);
            scheduleRepository.save(scheduledDisposition);
        }
        return schedules;
    }

    private static List<LocalDate> createDateObjectsBetween(LocalDate from, LocalDate to) {
        List<LocalDate> dateObjects = new ArrayList<>();

        dateObjects.add(from);

        long interval = DAYS.between(from, to);

        for (int i = 1; i < interval; i++) {
            LocalDate date = from.plusDays(i);
            dateObjects.add(date);
        }
        dateObjects.add(to);

        dateObjects.sort(LocalDate::compareTo);

        return dateObjects;
    }

}
