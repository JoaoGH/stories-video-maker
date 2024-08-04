package br.com.dark.svm

import br.com.dark.svm.enums.HistoriaOrigemEnum
import br.com.dark.svm.request.Request
import br.com.dark.svm.request.ResponseContent
import grails.gorm.transactions.Transactional
import grails.web.api.ServletAttributes
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

@Transactional
class RedditService implements ServletAttributes {

    HistoriaService historiaService

    Map getAmITheAsshole() {
        Map retorno = [:]

        Request request = new Request.Builder()
                .setUrl("https://www.reddit.com/r/EuSouOBabaca.json")
                .build()

        ResponseContent responseContent = request.execute()

        if (!responseContent.isSuccessful()) {
            retorno.success = false
            return retorno
        }

        retorno.data = []

        JSONElement response = responseContent.getBody()

        for (JSONObject it : response.data.children) {
            Historia h = historiaService.save(it.data as Map, HistoriaOrigemEnum.AM_I_THE_ASSHOLE)
            retorno.data << h
        }

        return retorno
    }
}
