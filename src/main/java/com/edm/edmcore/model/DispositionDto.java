package com.edm.edmcore.model;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DispositionDto {

    private UUID id;
    private LocalDate day;
    private String start;
    private String stop;
    private String employeeCode;

    @Override
    public String toString() {
        return "DispositionDto{" +
                "id=" + id +
                ", day=" + day +
                ", start='" + start + '\'' +
                ", stop='" + stop + '\'' +
                ", employeeCode='" + employeeCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositionDto that = (DispositionDto) o;
        return Objects.equal(id, that.id) && Objects.equal(day, that.day) && Objects.equal(start, that.start) && Objects.equal(stop, that.stop) && Objects.equal(employeeCode, that.employeeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, day, start, stop, employeeCode);
    }
}
