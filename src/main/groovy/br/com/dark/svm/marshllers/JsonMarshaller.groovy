package br.com.dark.svm.marshllers

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class JsonMarshaller {

    public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    public static final List CUSTOM_TYPES = [
            LocalDate, LocalDateTime, LocalTime
    ]

    static toFinalValue(value) {
        return "from${value.class.simpleName}"(value)
    }

    static String fromLocalDate(LocalDate value) {
        return value ? value.format(LOCAL_DATE_FORMATTER) : ""
    }

    static String fromLocalDateTime(LocalDateTime value) {
        return value ? value.format(LOCAL_DATE_TIME_FORMATTER) : ""
    }

}
