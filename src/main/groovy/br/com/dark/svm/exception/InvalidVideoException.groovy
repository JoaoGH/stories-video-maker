package br.com.dark.svm.exception

import org.springframework.http.HttpStatus

class InvalidVideoException extends AbstractApiException {

    InvalidVideoException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
