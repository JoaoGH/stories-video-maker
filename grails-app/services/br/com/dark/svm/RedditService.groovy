package br.com.dark.svm

import br.com.dark.svm.request.Request
import br.com.dark.svm.request.ResponseContent
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

@Transactional
class RedditService {

    Map getAmITheAsshole() {
        Map retorno = [:]

        Request request = new Request.Builder()
                .setUrl("https://www.reddit.com/r/EuSouOBabaca.json")
                .build()

        ResponseContent responseContent = request.execute()

        retorno.data = responseContent.getBody()

        return retorno
    }
}
