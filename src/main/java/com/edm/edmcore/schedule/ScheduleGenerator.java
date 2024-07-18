package com.edm.edmcore.schedule;

import com.edm.edmcore.client.DispositionClient;
import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.model.SchedulePeriod;
import com.edm.edmcore.model.ScheduleRequest;
import com.edm.edmcore.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleGenerator {
    private final DispositionClient dispositionClient;

    private final ScheduleUtil scheduleUtil;
    private final ScheduleRepository scheduleRepository;


    public Schedule generateSchedule(ScheduleRequest scheduleRequest) {

        Set<DispositionDto> scheduledDispositions = new LinkedHashSet<>();
        StringBuilder errorMessage = new StringBuilder();

        for (SchedulePeriod period : scheduleRequest.getSchedulePeriods()) {

            Set<DispositionDto> dispositions = dispositionClient.getAllDispositionByOrganizationCode(scheduleRequest.getOrganizationCode(), scheduleRequest.getDate());

            Schedule partialSchedule = scheduleUtil.generateSchedule(period, dispositions, scheduledDispositions);

            scheduledDispositions.addAll(partialSchedule.getDispositionDtos());
            errorMessage.append(partialSchedule.getErrorMessage());

        }

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .day(scheduleRequest.getDate())
                .errorMessage(errorMessage.toString())
                .dispositionDtos(scheduledDispositions)
                .build();

        scheduleRepository.save(schedule);

        return schedule;
    }


}
