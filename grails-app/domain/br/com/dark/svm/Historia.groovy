package br.com.dark.svm

import java.time.LocalDateTime

class Historia {

    UUID id
    String autor
    String titulo
    String conteudo
    String origem
    Locale idioma
    String status
    LocalDateTime dataCadastro
    LocalDateTime dataAtualizacao

    static mapping = {
        table 'historias'
        id generator: 'uuid2', type: 'pg-uuid'

        conteudo type: 'text'

        dynamicUpdate true
        version true
    }

    static constraints = {
        status maxSize: 1
        dataAtualizacao nullable: true
    }

    @Override
    String toString() {
        return "Historia: [id: ${id}, titulo: ${titulo}]"
    }

}
