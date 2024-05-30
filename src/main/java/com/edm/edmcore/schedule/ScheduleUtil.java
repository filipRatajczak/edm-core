package com.edm.edmcore.schedule;

import com.edm.edmcore.client.DispositionClient;
import com.edm.edmcore.exception.NotEnoughEmployeeException;
import com.edm.edmcore.exception.TooManyEmployeesException;
import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.DispositionRatioDto;
import com.edm.edmcore.model.Schedule;
import com.edm.edmcore.model.SchedulePeriod;
import com.edm.edmcore.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.edm.edmcore.util.Constant.CONST_FIFTEEN;
import static com.edm.edmcore.util.Constant.CONST_SIXTEEN;
import static com.edm.edmcore.util.DateUtil.parseStringToLocalTime;


@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleUtil {

    @Value("${edm.schedule.ratio-weight}")
    private Integer ratioWeight;
    @Value("${edm.schedule.min-working-hours}")
    private Integer minimumWorkingHours;
    private final DispositionClient dispositionClient;


    public Schedule generateSchedule(SchedulePeriod period, Set<DispositionDto> allDispositions, Set<DispositionDto> scheduledDispositions) {


        List<LocalTime> notEnoughPeriods = new LinkedList<>();

        System.out.println(period);

        LocalTime currentTime = LocalTime.from(period.getFrom());

        while (!currentTime.equals(period.getTo())) {
            try {
                Set<DispositionDto> checkedDispositions = checkCoverage(allDispositions, currentTime, period.getMinEmployees(), period.getMaxEmployees());
                scheduledDispositions.addAll(checkedDispositions);
            } catch (NotEnoughEmployeeException notEnough) {
                notEnoughPeriods.add(currentTime);
                scheduledDispositions.addAll(notEnough.getDispositionDtos());
                log.info(notEnough.getMessage());
            } catch (TooManyEmployeesException tooMany) {
                calculateAndAddTopFitnessToSchedule(scheduledDispositions, currentTime, tooMany.getDispositionDtos(), period.getMaxEmployees());
                cutAndRemoveBelowMinimumWorkingHours(scheduledDispositions, tooMany.getDispositionDtos(), currentTime, period.getMaxEmployees());
                moveStartHoursOnFitnessLoss(tooMany.getDispositionDtos(), currentTime, period.getMaxEmployees());
            }
            currentTime = currentTime.plusMinutes(CONST_FIFTEEN);
        }

        return Schedule.builder()
                .errorMessage(createErrorMessage(notEnoughPeriods))
                .dispositionDtos(scheduledDispositions)
                .build();
    }

    private void calculateAndAddTopFitnessToSchedule(Set<DispositionDto> scheduledDipositions,
                                                     LocalTime startTime,
                                                     Set<DispositionDto> conflictedDispositions, int maxEmployeeRule) {

        Set<DispositionRatioDto> ratio = calculateFitnessAndSortByRatio(conflictedDispositions, startTime);

        Set<DispositionDto> withHighestFitness = getDispositionsWithHighestFitness(conflictedDispositions, ratio, maxEmployeeRule);

        scheduledDipositions.addAll(withHighestFitness);
    }


    private Set<DispositionDto> checkCoverage(Set<DispositionDto> scheduledDipositions, LocalTime currentTime, int minEmployeeRule, int maxEmployeeRule) {
        int noOfEmployees = 0;
        Set<DispositionDto> dispositionsCompared = new LinkedHashSet<>();

        for (DispositionDto dispositionDto : scheduledDipositions) {
            if (employeeDispositionIsBetweenCheckInterval(currentTime, dispositionDto)) {
                dispositionsCompared.add(dispositionDto);
                noOfEmployees++;
            }
        }

        if (noOfEmployees < minEmployeeRule) {
            throw new NotEnoughEmployeeException("Not enough employees for: " + currentTime, dispositionsCompared);
        }

        if (noOfEmployees > maxEmployeeRule) {
            throw new TooManyEmployeesException("Too many employees for: " + currentTime, dispositionsCompared);
        }
        return dispositionsCompared;
    }


    private boolean employeeDispositionIsBetweenCheckInterval(LocalTime currentTime, DispositionDto dispositionDto) {
        return parseStringToLocalTime(dispositionDto.getStart()).equals(currentTime) ||
                (parseStringToLocalTime(dispositionDto.getStart()).isBefore(currentTime) &&
                        parseStringToLocalTime(dispositionDto.getStop()).isAfter(currentTime));
    }

    private Set<DispositionRatioDto> calculateFitnessAndSortByRatio(Set<DispositionDto> dispositions,
                                                                    LocalTime currentTime) {
        return dispositions.stream()
                .map(disposition -> {
                    BigDecimal hourlySurplus = DateUtil.calculateHourlySurplus(disposition, currentTime);
                    DispositionRatioDto dispositionRatioDto =
                            dispositionClient.getDispositionRatio(disposition.getEmployeeCode());
                    dispositionRatioDto.setRatio(hourlySurplus
                            .subtract(dispositionRatioDto.getRatio().multiply(BigDecimal.valueOf(ratioWeight))));
                    return dispositionRatioDto;
                }).sorted(Comparator.comparing(DispositionRatioDto::getRatio))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<DispositionDto> getDispositionsWithHighestFitness(Set<DispositionDto> dispositions,
                                                                  Set<DispositionRatioDto> dispositionRatioDtos, int maxEmployeeRule) {
        return dispositionRatioDtos.stream().map(e -> {
                    for (DispositionDto dispositionDto : dispositions) {
                        if (e.getEmployeeCode().equals(dispositionDto.getEmployeeCode())) {
                            return dispositionDto;
                        }
                    }
                    throw new RuntimeException(
                            "Disposition with employeeCode:[ " + e.getEmployeeCode() + "] not found!");
                })
                .limit(maxEmployeeRule)
                .collect(Collectors.toSet());
    }

    private Set<DispositionDto> getDispositionsWithLowestFitness(Set<DispositionDto> dispositionDtos,
                                                                 Set<DispositionRatioDto> dispositionRatioDtos, int maxEmployeeRule) {
        return dispositionRatioDtos.stream().map(e -> {
                    for (DispositionDto dispositionDto : dispositionDtos) {
                        if (e.getEmployeeCode().equals(dispositionDto.getEmployeeCode())) {
                            return dispositionDto;
                        }
                    }
                    throw new RuntimeException("Disposition with employeeCode:[ " + e.getEmployeeCode() + "] not found!");
                }).skip(maxEmployeeRule)
                .collect(Collectors.toSet());
    }

    private void cutAndRemoveBelowMinimumWorkingHours(Set<DispositionDto> scheduledDispositions,
                                                      Set<DispositionDto> conflicted,
                                                      LocalTime time, int maxEmployeeRule) {

        Set<DispositionRatioDto> dispositionRatioDtos = calculateFitnessAndSortByRatio(conflicted, time);
        Set<DispositionDto> dispositionsWithHighestFitness =
                getDispositionsWithHighestFitness(conflicted, dispositionRatioDtos, maxEmployeeRule);
        Set<DispositionDto> conflictedButInSchedule = findConflictedInSchedule(scheduledDispositions, conflicted);
        Set<DispositionDto> lostInFitnessButInSchedule =
                findLostInFitnessButInSchedule(dispositionsWithHighestFitness, conflictedButInSchedule);

        Set<DispositionDto> dispositionToRemove = lostInFitnessButInSchedule.stream()
                .filter(disposition -> checkIfWorkingTimeIsLowerThanMinimumWorkingHours(disposition, time))
                .collect(Collectors.toSet());

        removeDispositions(scheduledDispositions, dispositionToRemove);

    }

    private Set<DispositionDto> findLostInFitnessButInSchedule(Set<DispositionDto> dispositionsWithHighestFitness, Set<DispositionDto> conflictedButInSchedule) {
        Set<DispositionDto> lostDispositionInFitnessButInSchedule = new LinkedHashSet<>();

        conflictedButInSchedule.forEach(e -> {
            boolean foundInFitness = false;
            for (DispositionDto dispositionDto : dispositionsWithHighestFitness) {
                if (e.getEmployeeCode().equals(dispositionDto.getEmployeeCode())) {
                    foundInFitness = true;
                    break;
                }
            }
            if (!foundInFitness) {
                lostDispositionInFitnessButInSchedule.add(e);
            }
        });
        return lostDispositionInFitnessButInSchedule;
    }

    private Set<DispositionDto> findConflictedInSchedule(Set<DispositionDto> scheduledDisposotions,
                                                         Set<DispositionDto> conflicted) {
        Set<DispositionDto> conflictedButInSchedule = new LinkedHashSet<>();

        scheduledDisposotions.forEach(e -> {
            for (DispositionDto dispositionDto : conflicted) {
                if (e.getEmployeeCode().equals(dispositionDto.getEmployeeCode())) {
                    conflictedButInSchedule.add(dispositionDto);
                }
            }
        });
        return conflictedButInSchedule;
    }

    private boolean checkIfWorkingTimeIsLowerThanMinimumWorkingHours(DispositionDto disposition, LocalTime time) {
        Duration duration = Duration.between(parseStringToLocalTime(disposition.getStart()), time);
        double minutes = (double) duration.toMinutesPart() / CONST_SIXTEEN;
        double workingTime = (duration.toHours() + minutes);

        if (workingTime > minimumWorkingHours) {
            disposition.setStop(time.toString());
            return false;
        } else {
            return true;
        }
    }

    private void removeDispositions(Set<DispositionDto> allDispositions, Set<DispositionDto> dispositionsToRemove) {
        allDispositions.removeAll(dispositionsToRemove);
    }

    private void moveStartHoursOnFitnessLoss(Set<DispositionDto> conflicted, LocalTime time, int maxEmployeeRule) {

        Set<DispositionRatioDto> dispositionRatioDtos = calculateFitnessAndSortByRatio(conflicted, time);
        Set<DispositionDto> dispositionsWithLowestFitness =
                getDispositionsWithLowestFitness(conflicted, dispositionRatioDtos, maxEmployeeRule);

        dispositionsWithLowestFitness.forEach(disposition -> {
            Duration duration = Duration.between(time, parseStringToLocalTime(disposition.getStop()));
            double workingTime = duration.toHours() + (double) (duration.toMinutesPart() / CONST_SIXTEEN);
            if (workingTime > minimumWorkingHours) {
                log.info("Moved start hours to: [{}], for employee with code: [{}]", time,
                        disposition.getEmployeeCode());
                disposition.setStart(time.toString());
            }
        });
    }

    private String createErrorMessage(List<LocalTime> notEnoughPeriods) {

        StringBuilder errorMsg = new StringBuilder();

        if (!notEnoughPeriods.isEmpty()) {

            notEnoughPeriods.sort(LocalTime::compareTo);

            LocalTime start = notEnoughPeriods.get(0);
            LocalTime prev = notEnoughPeriods.get(0);

            for (int i = 1; i < notEnoughPeriods.size(); i++) {
                LocalTime now = notEnoughPeriods.get(i);
                if (!prev.plusMinutes(CONST_FIFTEEN).equals(now)) {
                    errorMsg.append(String.format("Not enough employees from: %s to %s\n", start,
                            prev.plusMinutes(CONST_FIFTEEN)));
                    start = now;
                    prev = now;
                } else if (start != notEnoughPeriods.get(0) && i == notEnoughPeriods.size() - 1) {
                    return String.format("Not enough employees from: %s to %s\n", start,
                            now.plusMinutes(CONST_FIFTEEN));
                } else if (start == notEnoughPeriods.get(0) && i == notEnoughPeriods.size() - 1) {
                    return String.format("Not enough employees from: %s to %s\n", start,
                            now.plusMinutes(CONST_FIFTEEN));
                } else {
                    prev = prev.plusMinutes(CONST_FIFTEEN);
                }
            }
        } else {
            errorMsg = new StringBuilder("No errors in schedule\n");
        }

        return errorMsg.toString();
    }

}


