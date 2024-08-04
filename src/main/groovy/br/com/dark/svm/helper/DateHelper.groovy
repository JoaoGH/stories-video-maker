package br.com.dark.svm.helper

import java.time.Instant
import java.time.LocalDateTime

class DateHelper {

    static LocalDateTime getLocalDateTimeByEpochSeconds(Long timeSeconds) {
        LocalDateTime.ofInstant(Instant.ofEpochSecond(timeSeconds), TimeZone.getDefault().toZoneId())
    }

}
