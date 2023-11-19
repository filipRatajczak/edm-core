package com.edm.edmcore.util.sorter;

import com.edm.edmcore.client.DispositionClient;
import com.edm.edmcore.exception.NotEnoughEmployeeException;
import com.edm.edmcore.exception.TooManyEmployeesException;
import com.edm.edmcore.model.DispositionDto;
import com.edm.edmcore.model.DispositionRatioDto;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.edm.edmcore.util.DateUtil.parseStringToLocalTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleUtil {

    @Value("${edm.schedule.min-employees}")
    private Integer minEmployeeRule;
    @Value("${edm.schedule.max-employees}")
    private Integer maxEmployeeRule;
    @Value("${edm.schedule.ratio-weight}")
    private Integer ratioWeight;
    @Value("${edm.schedule.min-working-hours}")
    private Integer minimumWorkingHours;
    private final DispositionClient dispositionClient;

    public Set<DispositionDto> checkScheduleCoverage(Set<DispositionDto> dispositionDtos) {

        LocalTime compare = LocalTime.of(8, 0);

        Set<DispositionDto> result = new LinkedHashSet<>();

        while (!compare.equals(LocalTime.of(22, 0))) {

            try {
                Set<DispositionDto> c = checkCoverage(dispositionDtos, compare);
                result.addAll(c);
            } catch (NotEnoughEmployeeException notEnough) {
                log.info(notEnough.getMessage() + result);
            } catch (TooManyEmployeesException tooMany) {

                Set<DispositionRatioDto> dispositionRatioDtos = calculateFitness(tooMany.getDispositionDtos(), compare);

                Set<DispositionDto> dispositionsWithHighestFitness = getDispositionsWithHighestFitness(tooMany.getDispositionDtos(), dispositionRatioDtos);

                result.addAll(dispositionsWithHighestFitness);

                Set<DispositionDto> dispositionsToBeRemoved = cutStopHours(result, tooMany.getDispositionDtos(), compare);

                removeDispositions(result, dispositionsToBeRemoved);

                moveStartHours(tooMany.getDispositionDtos(), compare);

                log.info("After calculations for time: [{}], [{}] ", compare, result);
            }

            compare = compare.plusMinutes(15L);

        }
        return result;

    }

    private Set<DispositionDto> checkCoverage(Set<DispositionDto> dispositionDtos, LocalTime compare) {
        int noOfEmployees = 0;
        Set<DispositionDto> dispositionsCompared = new LinkedHashSet<>();

        for (DispositionDto dispositionDto : dispositionDtos) {
            if (employeeDispositionIsBetweenCheckInterval(compare, dispositionDto)) {
                dispositionsCompared.add(dispositionDto);
                noOfEmployees++;
            }
        }

        if (noOfEmployees < minEmployeeRule) {
            throw new NotEnoughEmployeeException("Not enough employees for: " + compare, dispositionsCompared);
        }

        if (noOfEmployees > maxEmployeeRule) {
            throw new TooManyEmployeesException("Too many employees for: " + compare, dispositionsCompared);
        }
        return dispositionsCompared;
    }

    private boolean employeeDispositionIsBetweenCheckInterval(LocalTime compare, DispositionDto dispositionDto) {
        return parseStringToLocalTime(dispositionDto.getStart()).equals(compare) ||
                (parseStringToLocalTime(dispositionDto.getStart()).isBefore(compare) &&
                        parseStringToLocalTime(dispositionDto.getStop()).isAfter(compare));
    }

    private Set<DispositionRatioDto> calculateFitness(Set<DispositionDto> conflicted, LocalTime time) {

        return conflicted.stream()
                .map(e -> {
                    BigDecimal hourlySurplus = DateUtil.calculateHourlySurplus(e, time);
                    DispositionRatioDto dispositionRatioDto = dispositionClient.getDispositionRatio(e.getEmployeeCode());
                    dispositionRatioDto.setRatio(hourlySurplus
                            .subtract(dispositionRatioDto.getRatio().multiply(BigDecimal.valueOf(ratioWeight))));
                    return dispositionRatioDto;
                }).sorted(Comparator.comparing(DispositionRatioDto::getRatio))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<DispositionDto> getDispositionsWithHighestFitness(Set<DispositionDto> dispositionDtos, Set<DispositionRatioDto> dispositionRatioDtos) {
        return dispositionRatioDtos.stream().map(e -> {
                    for (DispositionDto dispositionDto : dispositionDtos) {
                        if (e.getEmployeeCode().equals(dispositionDto.getEmployeeCode())) {
                            return dispositionDto;
                        }
                    }
                    throw new RuntimeException("Disposition with employeeCode:[ " + e.getEmployeeCode() + "] not found!");
                }).limit(maxEmployeeRule)
                .collect(Collectors.toSet());
    }

    private Set<DispositionDto> getDispositionsWithLowestFitness(Set<DispositionDto> dispositionDtos, Set<DispositionRatioDto> dispositionRatioDtos) {
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

    private Set<DispositionDto> cutStopHours(Set<DispositionDto> all, Set<DispositionDto> conflicted, LocalTime time) {

        Set<DispositionRatioDto> dispositionRatioDtos = calculateFitness(conflicted, time);
        Set<DispositionDto> dispositionsWithHighestFitness = getDispositionsWithHighestFitness(conflicted, dispositionRatioDtos);

        Set<DispositionDto> conflictedButInSchedule = new LinkedHashSet<>();

        all.forEach(e -> {
            for (DispositionDto dispositionDto : conflicted) {
                if (e.getEmployeeCode().equals(dispositionDto.getEmployeeCode())) {
                    conflictedButInSchedule.add(dispositionDto);
                }
            }
        });

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

        return lostDispositionInFitnessButInSchedule.stream().filter(e -> {

            Duration duration = Duration.between(parseStringToLocalTime(e.getStart()), time);
            double result = duration.toHours() + (duration.toMinutesPart() / 60d);

            if (result > minimumWorkingHours) {
                e.setStop(time.toString());
                return false;
            }
            return true;
        }).collect(Collectors.toSet());

    }

    private void removeDispositions(Set<DispositionDto> all, Set<DispositionDto> dispositionsToBeRemoved) {
        all.removeAll(dispositionsToBeRemoved);
    }

    private void moveStartHours(Set<DispositionDto> conflicted, LocalTime time) {

        Set<DispositionRatioDto> dispositionRatioDtos = calculateFitness(conflicted, time);
        Set<DispositionDto> dispositionsWithLowestFitness = getDispositionsWithLowestFitness(conflicted, dispositionRatioDtos);

        dispositionsWithLowestFitness.forEach(e -> {
            Duration duration = Duration.between(time, parseStringToLocalTime(e.getStop()));
            double result = duration.toHours() + (duration.toMinutesPart() / 60d);
            if (result > 2.) {
                log.info("Moved start hours to: [{}], for employee with code: [{}]", time, e.getEmployeeCode());
                e.setStart(time.toString());
            }
        });
    }

}
