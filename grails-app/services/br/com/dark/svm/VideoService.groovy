package br.com.dark.svm

import br.com.dark.svm.tts.TiktokTTS
import br.com.dark.svm.tts.Voice
import grails.gorm.transactions.Transactional
import grails.web.api.ServletAttributes
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class VideoService implements ServletAttributes {

    HistoriaService historiaService

    void createVideo(String videoName) {
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + videoName

        if (!Files.exists(Path.of(videoBase))) {
            throw new Exception("Sem arquivo de video para uso.")
        }

        Historia historia = historiaService.getNextHistoria()

        File dir = new File(ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}")
        dir.mkdir()
        String sessionId = getSessionId()
        File outputTitulo = Paths.get(dir.getAbsolutePath(), "titulo.mp3").toFile()
        TiktokTTS ttsTitulo = new TiktokTTS(sessionId, Voice.PORTUGUESE_BR_MALE, historia.titulo, outputTitulo)
        ttsTitulo.createAudioFile()
        File outputConteudo = Paths.get(dir.getAbsolutePath(), "conteudo.mp3").toFile()
        TiktokTTS ttsConteudo = new TiktokTTS(sessionId, Voice.PORTUGUESE_BR_MALE, historia.conteudo, outputConteudo)
        ttsConteudo.createAudioFile()
    }

    String getSessionId() {
        return params.sessionId
    }

}
