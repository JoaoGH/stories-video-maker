package br.com.dark.svm


import grails.rest.*
import grails.converters.*
import org.springframework.http.HttpStatus

class RedditController {

    RedditService redditService

	static responseFormats = ['json', 'xml']
	
    def index() {
        Map retorno = redditService.getAmITheAsshole()
        respond(retorno, status: HttpStatus.OK)
    }
}
