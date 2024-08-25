package br.com.dark.svm

import br.com.dark.svm.command.VideoCommand
import br.com.dark.svm.exception.ControllerExceptionHandler
import br.com.dark.svm.exception.ParametersException
import br.com.dark.svm.helper.JsonHelper
import org.springframework.http.HttpStatus

class VideoController implements ControllerExceptionHandler {

    VideoService videoService

    static responseFormats = ['json']

    static allowedMethods = [
            create: "POST"
    ]
	
    def create(VideoCommand command) {
        if (!command.validate()) {
            throw new ParametersException(command.errors)
        }
        Map retorno = [success: true]
        videoService.createVideo(command)
        respond(JsonHelper.toJSONObject(retorno), HttpStatus.OK)
    }
}
