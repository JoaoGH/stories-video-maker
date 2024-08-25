package br.com.dark.svm

import br.com.dark.svm.marshllers.JsonMarshaller
import grails.converters.JSON

class BootStrap {

    def init = { servletContext ->
        configureMashallers()
    }
    def destroy = {
    }

    private void configureMashallers() {
        for (type in JsonMarshaller.CUSTOM_TYPES) {
            JSON.registerObjectMarshaller(type) {
                JsonMarshaller.toFinalValue(it)
            }
        }
    }
}
