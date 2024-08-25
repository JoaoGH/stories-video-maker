package br.com.dark.svm.command

import br.com.dark.svm.enums.BackgroundVideoEnum
import br.com.dark.svm.enums.HistoriaOrigemEnum
import grails.validation.Validateable

class VideoCommand implements Validateable {

    Long id
    String sessionId
    String origem
    Boolean shorts
    String background

    static constraints = {
        id(nullable: true)
        sessionId(nullable: false, blank: false)
        origem(nullable: false, blank: false, inList: HistoriaOrigemEnum.values()*.value)
        background(nullable: true, blank: false, inList: BackgroundVideoEnum.values()*.value)
        shorts(nullable: false)
    }

}
