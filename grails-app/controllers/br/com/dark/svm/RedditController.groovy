package br.com.dark.svm

import br.com.dark.svm.exception.ControllerExceptionHandler
import br.com.dark.svm.helper.JsonHelper
import org.springframework.http.HttpStatus

class RedditController implements ControllerExceptionHandler {

    RedditService redditService

	static responseFormats = ['json']
	
    def index() {
        respond([:], status: HttpStatus.OK)
    }

    def save() {
        Map retorno = redditService.getAmITheAsshole()
        respond(JsonHelper.toJSONObject(retorno), status: HttpStatus.OK)
    }
}
