package br.com.dark.svm

import br.com.dark.svm.marshllers.JsonMarshaller
import grails.converters.JSON

class BootStrap {

    VideoService videoService

    def init = { servletContext ->
        configureMashallers()
        videoService.prepareScenario()
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
