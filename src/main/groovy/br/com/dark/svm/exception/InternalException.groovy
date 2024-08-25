package br.com.dark.svm.exception

import org.springframework.http.HttpStatus

class InternalException extends AbstractApiException {

    InternalException(Exception e) {
        super(e, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
