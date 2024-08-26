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

    List<Historia> list() {
        return list([:])
    }

    List<Historia> list(Map filters) {
        List<Historia> retorno = Historia.createCriteria().list {
            if (filters?.status) {
                eq('status', filters.status)
            }
        }

        return retorno
    }

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

    Historia getNextHistoria(HistoriaOrigemEnum origem) {
        List<Historia> historias = Historia.createCriteria().list {
            eq('origem', origem.getValue())
            eq('status', HistoriaStatusEnum.OBTIDA.getValue())
        }

        if (historias.isEmpty()) {
            throw new NotFoundException("Sem historias salvas no banco.")
        }

        return historias.get(0)
    }

    Historia get(Long id) {
        return Historia.get(id)
    }

}
