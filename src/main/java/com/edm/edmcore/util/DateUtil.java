package com.edm.edmcore.util;

import com.edm.edmcore.model.DispositionDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class DateUtil {


    public static LocalTime parseStringToLocalTime(String time) {
        int timeO1Hours = Integer.parseInt(time.split(":")[0]);
        int timeO1Minutes = Integer.parseInt(time.split(":")[1]);
        return LocalTime.of(timeO1Hours, timeO1Minutes);
    }

    public static BigDecimal calculateHourlySurplus(LocalTime startTime, LocalTime stopTime) {
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(startTime, stopTime))
                .divide(BigDecimal.valueOf(60L), RoundingMode.UNNECESSARY);
    }

}
