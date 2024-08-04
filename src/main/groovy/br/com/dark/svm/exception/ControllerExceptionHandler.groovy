package br.com.dark.svm.exception

import grails.artefact.controller.RestResponder

trait ControllerExceptionHandler implements RestResponder {

    Object handleException(Exception exception) {
        InternalException e = new InternalException(exception)
        return respond(e.toJSON(), status: e.getStatus())
    }

}