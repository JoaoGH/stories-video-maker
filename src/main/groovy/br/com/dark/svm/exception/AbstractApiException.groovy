package br.com.dark.svm.exception

import br.com.dark.svm.helper.JsonHelper
import grails.web.api.ServletAttributes
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

abstract class AbstractApiException extends Exception implements ServletAttributes {

    private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    private HttpStatus status = HttpStatus.BAD_REQUEST
    private String path = request.getRequestURI()

    AbstractApiException(String message) {
        super(message)
    }

    AbstractApiException(String message, HttpStatus status) {
        this(message)
        this.status = status
    }

    AbstractApiException(Exception e) {
        super(e)
    }

    AbstractApiException(Exception e, HttpStatus status) {
        this(e)
        this.status = status
    }

    JSONObject toJSON() {
        return JsonHelper.toJSONObject([
                message  : this.message,
                timestamp: this.timestamp,
                status   : this.status.value(),
                path     : this.path,
                success  : false
        ])
    }

    HttpStatus getStatus() {
        return status
    }

}
