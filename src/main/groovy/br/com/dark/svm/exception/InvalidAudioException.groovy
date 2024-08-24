package br.com.dark.svm.exception

import org.springframework.http.HttpStatus

class InvalidAudioException extends AbstractApiException {

    InvalidAudioException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    InvalidAudioException(Exception e) {
        super(e, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
