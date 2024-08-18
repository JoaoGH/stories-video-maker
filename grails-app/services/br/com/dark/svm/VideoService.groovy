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

        concatAudios(outputTitulo.absolutePath, outputConteudo.absolutePath, historia.id)

    }

    void concatAudios(String titulo, String conteudo, Long id) {
        String swipe = ApplicationConfig.getVideoBasePath() + "/swipe.mp3"
        String pausa = ApplicationConfig.getVideoBasePath() + "/pausa.mp3"
        String finalAudio = ApplicationConfig.getVideoBasePath() + "/historia_${id}/audio_final.mp3"

        FileOutputStream outputStream = new FileOutputStream(finalAudio)

        [swipe, titulo, swipe, conteudo, pausa].each { String mp3FilePath ->
            byte[] mp3Bytes = Files.readAllBytes(Paths.get(mp3FilePath))
            outputStream.write(mp3Bytes)
        }

        outputStream.close()
    }

    String getSessionId() {
        return params.sessionId
    }

}
