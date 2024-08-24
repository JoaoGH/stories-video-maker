package br.com.dark.svm.exception

import grails.artefact.controller.RestResponder
import groovy.util.logging.Slf4j

@Slf4j
trait ControllerExceptionHandler implements RestResponder {

    Object handleException(Exception exception) {
        InternalException e = new InternalException(exception)
        log.error(e.message, e)
        return respond(e.toJSON(), status: e.getStatus())
    }

}