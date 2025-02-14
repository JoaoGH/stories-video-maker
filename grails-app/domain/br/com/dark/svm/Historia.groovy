package br.com.dark.svm

import java.time.LocalDateTime

class Historia {

    Long id
    String titulo
    String conteudo
    String origem
    Locale idioma
    String status
    LocalDateTime dataHoraBusca
    LocalDateTime dataHoraCriacao

    static mapping = {
        table 'historias'
        id generator: 'sequence'

        conteudo type: 'text'

        dynamicUpdate true
        version true
    }

    static constraints = {
        status maxSize: 1
        dataHoraCriacao nullable: true
    }

    @Override
    String toString() {
        return "Historia: [id: ${id}, titulo: ${titulo}]"
    }

}
