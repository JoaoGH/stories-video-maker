package br.com.dark.svm

import br.com.dark.svm.enums.HistoriaOrigemEnum
import br.com.dark.svm.enums.HistoriaStatusEnum
import br.com.dark.svm.helper.TextHelper
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBindingUtils
import javassist.NotFoundException
import java.time.LocalDateTime

@Transactional
class HistoriaService {

    Historia save(Map data, HistoriaOrigemEnum origem) {
        Historia record = new Historia()
        record.setTitulo(TextHelper.removerEmoji(data.title.toString()))
        record.setConteudo(TextHelper.removerEmoji(data.selftext.toString()))
        record.setOrigem(origem.getValue())
        record.setIdioma(new Locale("pt", "BR"))
        record.setStatus(HistoriaStatusEnum.OBTIDA.getValue())
        record.setDataHoraBusca(LocalDateTime.now())

        record.save(flush: true, failOnError: true)

        return record
    }

    Historia update(Map parameters, Historia historia) {
        DataBindingUtils.bindObjectToInstance(historia, parameters)
        historia.save(flush: true)

        return historia
    }

    Historia getNextHistoria() {
        List<Historia> historias = Historia.createCriteria().list {
            eq('status', HistoriaStatusEnum.OBTIDA.getValue())
        }

        if (historias.isEmpty()) {
            throw new NotFoundException("Sem historias salvas no banco.")
        }

        return historias.get(0)
    }

}
