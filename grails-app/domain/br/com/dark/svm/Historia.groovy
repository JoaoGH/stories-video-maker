package br.com.dark.svm

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    String getPath() {
        String dataFormatada = dataCadastro.format(DateTimeFormatter.ISO_DATE_TIME)

        StringBuilder path = new StringBuilder()
        path << ApplicationConfig.getVideoBasePath() + "/"
        path << origem.toLowerCase() + "/"
        path << dataFormatada + "_" + id.toString()

        return path.toString()
    }

}
