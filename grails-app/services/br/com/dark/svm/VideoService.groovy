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
            println "[ERROR] ${error.toString()}"
        }
        assert !proc.exitValue()

        return output.toString()
    }

    void createVideo(String videoName) {
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + videoName

        if (!Files.exists(Path.of(videoBase))) {
            throw new Exception("Sem arquivo de video para uso.")
        }

        Historia historia = historiaService.getNextHistoria()

        File dir = new File(ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}")
        dir.mkdir()
        String path = dir.absolutePath
        String sessionId = getSessionId()
        File outputTitulo = Paths.get(path, "titulo.mp3").toFile()
        TiktokTTS ttsTitulo = new TiktokTTS(sessionId, Voice.PORTUGUESE_BR_MALE, historia.titulo, outputTitulo)
        ttsTitulo.createAudioFile()
        File outputConteudo = Paths.get(path, "conteudo.mp3").toFile()
        TiktokTTS ttsConteudo = new TiktokTTS(sessionId, Voice.PORTUGUESE_BR_MALE, historia.conteudo, outputConteudo)
        ttsConteudo.createAudioFile()

        String audioFinal = concatAudios(outputTitulo.absolutePath, outputConteudo.absolutePath, historia.id)

        Integer tamanhoFinal = getTamanhoAudio(audioFinal)
        String videoOut = path + "/video.mp4"
        cut(videoBase, videoOut, 0, tamanhoFinal)

        addAudiosIntoVideo(videoOut, audioFinal, path)

        removeUsedTimeFromBase(videoBase, tamanhoFinal, tamanhoTotalVideo)

        List<String> videosSegmentados = segmentVideo(videoOut, tamanhoFinal, historia.id)

        String image = createImage(historia)

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

    Integer getTamanhoAudio(String audio) {
        BigDecimal durationInSeconds = BigDecimal.ZERO

        FileInputStream fileInputStream = new FileInputStream(audio)
        Bitstream bitstream = new Bitstream(fileInputStream)
        Header header

        try {
            while ((header = bitstream.readFrame()) != null) {
                durationInSeconds += header.ms_per_frame() / 1000.0
                bitstream.closeFrame()
            }
        } catch (JavaLayerException e) {
            e.printStackTrace()
            throw new Exception("Erro ao calcular o tamanho final do audio.")
        } finally {
            fileInputStream.close()
        }

        return durationInSeconds.toInteger()
    }

    String concatAudios(String titulo, String conteudo, Long id) {
        String swipe = ApplicationConfig.getVideoBasePath() + "/swipe.mp3"
        String pausa = ApplicationConfig.getVideoBasePath() + "/pausa.mp3"
        String finalAudio = ApplicationConfig.getVideoBasePath() + "/historia_${id}/audio_final.mp3"

        FileOutputStream outputStream = new FileOutputStream(finalAudio)

        [swipe, titulo, swipe, conteudo, pausa].each { String mp3FilePath ->
            byte[] mp3Bytes = Files.readAllBytes(Paths.get(mp3FilePath))
            outputStream.write(mp3Bytes)
        }

        outputStream.close()

        return finalAudio
    }

    void cut(String video, String output, Integer tempoInicial, Integer tempoFinal) {
        String comando = "HandBrakeCLI -i ${video} -o ${output} --start-at duration:${tempoInicial} --stop-at duration:${tempoFinal}"
        runCommand(comando)
    }

    void addAudiosIntoVideo(String video, String audio, String path) {
        String temp = path + '/temp-' + video.split("/").last()
        runCommand("ffmpeg -i ${video} -i ${audio} -c:v copy -c:a aac -strict experimental ${temp}")
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

    String getSessionId() {
        return params.sessionId
    }

}
