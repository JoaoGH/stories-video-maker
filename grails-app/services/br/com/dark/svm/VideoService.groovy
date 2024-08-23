package br.com.dark.svm

import br.com.dark.svm.media.Audio
import br.com.dark.svm.tts.Voice
import grails.gorm.transactions.Transactional
import grails.web.api.ServletAttributes
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Path

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
            println "[ERROR] ${error.toString()}"
        }
        assert !proc.exitValue()

        return output.toString()
    }

    void createVideo(String videoName) {
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + videoName
        Integer tamanhoVideoBase = getTamanhoVideo(videoBase)

        if (!Files.exists(Path.of(videoBase))) {
            throw new Exception("Sem arquivo de video para uso.")
        }

        Historia historia = historiaService.getNextHistoria()

        File dir = new File(ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}")
        dir.mkdir()

        String path = dir.absolutePath
        String sessionId = getSessionId()

        Audio titulo = new Audio(path + "/titulo.mp3", historia.titulo)
        titulo.setVoz(Voice.PORTUGUESE_BR_MALE)
        titulo.createAudioFileTTS(sessionId)

        Audio conteudo = new Audio(path + "/conteudo.mp3", historia.conteudo)
        conteudo.setVoz(Voice.PORTUGUESE_BR_MALE)
        conteudo.createAudioFileTTS(sessionId)

        Audio swipe = new Audio(ApplicationConfig.getVideoBasePath() + "/swipe.mp3")
        Audio pausa = new Audio(ApplicationConfig.getVideoBasePath() + "/pausa.mp3")

        Audio audioFinal = new Audio(path + "/audio_final.mp3")
        audioFinal.concat([swipe, titulo, swipe, conteudo, pausa])

        BigDecimal tamanhoFinal = audioFinal.getDuracao()
        String videoOut = path + "/video.mp4"
        cut(videoBase, videoOut, 0, tamanhoFinal.toInteger())

        addAudiosIntoVideo(videoOut, audioFinal.path, path)

        removeUsedTimeFromBase(videoBase, tamanhoFinal.toInteger(), tamanhoVideoBase)

        String image = createImage(historia)

        BigDecimal tempoTitulo = titulo.duracao + swipe.duracao

        insertImageIntoVideo(path, videoOut, image, tempoTitulo)
    }

    Integer getTamanhoVideo(String video) {
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

    void cut(String video, String output, Integer tempoInicial, Integer tempoFinal) {
        String comando = "HandBrakeCLI -i ${video} -o ${output} --start-at duration:${tempoInicial} --stop-at duration:${tempoFinal}"
        runCommand(comando)
    }

    void addAudiosIntoVideo(String video, String audio, String path) {
        String temp = path + '/temp-' + video.split("/").last()
        StringBuilder command = new StringBuilder()
        command.append("ffmpeg")
        command.append(" -i ${video}")
        command.append(" -i ${audio}")
        command.append(" -c:v copy -c:a aac")
        command.append(" -strict experimental ${temp}")
        runCommand(command.toString())
        new File(video).delete()
        new File(temp).renameTo(video)
    }

    void removeUsedTimeFromBase(String videoBase, Integer inicioCorte, Integer fimCorte) {
        String nomeVideo = videoBase.split("/").last()
        String temp = ApplicationConfig.getVideoBasePath() + "/temp-" + nomeVideo
        cut(videoBase, temp, inicioCorte, fimCorte)
        new File(videoBase).delete()
        new File(temp).renameTo(videoBase)
    }

    /**
     * Cria uma lista com o tempo
     *
     * */
    List<Integer> getSegmentVideoSize(Integer tempoTotal) {
        List<Integer> segments = []
        Integer tempoMaximo = 50

        if (tempoTotal <= tempoMaximo) {
            return [tempoTotal]
        }

        // Calcula o número ideal de segmentos necessários para obter segmentos semelhantes
        Integer totalSegmentosVideo = (Integer) Math.ceil((double) tempoTotal / tempoMaximo)

        // Calcula o tempo base de cada segmento
        Integer tempoBaseSegmento = (Integer) (tempoTotal / totalSegmentosVideo)

        // Calcula o tempo restante a ser distribuído
        Integer tempoRestante = tempoTotal % totalSegmentosVideo

        // Cria os segmentos, distribuindo o tempo restante uniformemente
        for (int i = 0; i < totalSegmentosVideo; i++) {
            Integer duracaoSegmento = tempoBaseSegmento
            if (tempoRestante > 0) {
                duracaoSegmento += 1
                tempoRestante--
            }
            segments.add(duracaoSegmento)
        }

        return segments
    }

    List<String> segmentVideo(String video, Integer tempoVideo, Long idHistoria) {
        List<Integer> tempoPorVideo = getSegmentVideoSize(tempoVideo)

        List<String> videos = []
        Integer tempoInicial = 0
        tempoPorVideo.eachWithIndex{ Integer tempo, Integer i ->
            String novoVideo = ApplicationConfig.getVideoBasePath() + "/historia_${idHistoria}/${i+1}_video.mp4"
            cut(video, novoVideo, tempoInicial, tempo)
            videos.add(novoVideo)
            tempoInicial += tempo - 1
        }

        new File(video).delete()

        return videos
    }

    String createImage(Historia historia) {
        File dir = new File(ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}")
        String finalImage = "${dir.absolutePath}/image.png"

        String titulo = historia.titulo.bytes.encodeBase64().toString()

        StringBuilder imagem = new StringBuilder()
        imagem.append("python3 src/main/python/br/com/dark/svm/create_image.py")
        imagem.append(" ${ApplicationConfig.getRedditBaseImagePath()}")
        imagem.append(" ${finalImage}")
        imagem.append(" ${titulo}")
        imagem.append(" /usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")
        imagem.append(" 20")

        runCommand(imagem.toString())

        return finalImage
    }

    void insertImageIntoVideo(String path, String video, String image, BigDecimal tempoTitulo) {
        String temp = path + "/temp-" + video.split("/").last()

        StringBuilder command = new StringBuilder()
        command.append("python3 src/main/python/br/com/dark/svm/add_image.py")
        command.append(" ${video}")
        command.append(" ${image}")
        command.append(" ${tempoTitulo.setScale(2, RoundingMode.CEILING)}")
        command.append(" ${temp}")

        runCommand(command.toString())

        new File(video).delete()
        new File(temp).renameTo(video)
    }

    String getSessionId() {
        return params.sessionId
    }

}
