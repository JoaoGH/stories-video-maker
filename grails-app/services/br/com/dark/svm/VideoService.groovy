package br.com.dark.svm

import br.com.dark.svm.tts.TiktokTTS
import br.com.dark.svm.tts.Voice
import grails.gorm.transactions.Transactional
import grails.web.api.ServletAttributes
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.JavaLayerException

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class VideoService implements ServletAttributes {

    HistoriaService historiaService

    /**
     * Uma Closure que realiza a execução de uma string. <br/>
     * Pode receber uma string ou uma lista de strings (para argumentos com espaços). <br/>
     * Imprime toda a saída, avisa para em caso de erro.
     *
     * */
    Closure runCommand = { strList ->
        assert (strList instanceof String || (strList instanceof List && strList.each({ it instanceof String })))
        Process proc = strList.execute()

        StringBuilder output = new StringBuilder()
        StringBuilder error = new StringBuilder()

        proc.inputStream.eachLine { String line ->
            println(line)
            output.append(line).append('\n')
        }
        proc.errorStream.eachLine { String line ->
            println(line)
            error.append(line).append('\n')
        }

        proc.out.close()
        proc.waitFor()

        print "[INFO] ( "
        if (strList instanceof List) {
            strList.each { print "${it} " }
        } else {
            print strList
        }
        println " )"

        if (proc.exitValue()) {
            println "gave the following error: "
            println "[ERROR] ${proc.getErrorStream()}"
        }
        assert !proc.exitValue()

        return output.toString()
    }

    void createVideo(String videoName) {
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + videoName

        if (!Files.exists(Path.of(videoBase))) {
            throw new Exception("Sem arquivo de video para uso.")
        }

        Integer tamanhoTotalVideo = getTamanhoTotalVideo(videoBase)

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
        Integer tamanhoFinal = tamanhoHistoria(historia.id)

    }

    Integer getTamanhoTotalVideo(String video) {
        String command = "ffprobe -v error -select_streams v:0 -show_entries stream=duration -of default=noprint_wrappers=1:nokey=1 ${video}"
        String retornoComando = runCommand(command)

        if (!retornoComando) {
            throw new Exception("Não foi possível identificar o tamanho do vídeo original.")
        }

        Integer retorno = (retornoComando as BigDecimal)?.toInteger()

        if (!retorno) {
            throw new Exception("Não foi possível identificar o tamanho do vídeo original.")
        }

        return retorno
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

    Integer tamanhoHistoria(Long id) {
        String finalAudio = ApplicationConfig.getVideoBasePath() + "/historia_${id}/audio_final.mp3"

        BigDecimal durationInSeconds = BigDecimal.ZERO
        def fileInputStream = new FileInputStream(finalAudio)
        def bitstream = new Bitstream(fileInputStream)
        Header header

        try {
            while ((header = bitstream.readFrame()) != null) {
                durationInSeconds += header.ms_per_frame() / 1000.0
                bitstream.closeFrame()
            }
        } catch (JavaLayerException e) {
            e.printStackTrace()
            throw new Exception("Erro ao calcular o tamanho final da história.")
        } finally {
            fileInputStream.close()
        }

        return durationInSeconds.toInteger()
    }

    String getSessionId() {
        return params.sessionId
    }

}
