package br.com.dark.svm

import br.com.dark.svm.command.VideoCommand
import br.com.dark.svm.exception.ControllerExceptionHandler
import br.com.dark.svm.exception.ParametersException
import br.com.dark.svm.helper.JsonHelper
import org.springframework.http.HttpStatus

class VideoController implements ControllerExceptionHandler {

    VideoService videoService
    ScenarioService scenarioService

    static responseFormats = ['json']

    static allowedMethods = [
            create: "POST",
            prepareScenario: "PATCH",
            makeShorts: "PUT"
    ]
	
    def create(VideoCommand command) {
        if (!command.validate()) {
            throw new ParametersException(command.errors)
        }
        Map retorno = videoService.createVideo(command)
        respond(JsonHelper.toJSONObject(retorno), status: HttpStatus.OK)
    }

    def prepareScenario() {
        Map retorno = scenarioService.prepareScenario()
        respond(JsonHelper.toJSONObject(retorno), status: HttpStatus.OK)
    }

    def makeShorts() {
        Map retorno = videoService.makeShorts(params.long('id'))
        respond(JsonHelper.toJSONObject(retorno), status: HttpStatus.OK)
    }

}
