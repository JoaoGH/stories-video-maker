package br.com.dark.svm.exception

import org.springframework.http.HttpStatus

class InvalidVideo extends AbstractApiException {

    InvalidVideo(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
