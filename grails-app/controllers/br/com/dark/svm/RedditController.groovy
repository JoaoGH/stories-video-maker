package br.com.dark.svm


import grails.rest.*
import grails.converters.*
import org.springframework.http.HttpStatus

class RedditController {

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
