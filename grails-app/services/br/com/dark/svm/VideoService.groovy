package br.com.dark.svm

import br.com.dark.svm.media.Audio
import br.com.dark.svm.media.Image
import br.com.dark.svm.media.Video
import br.com.dark.svm.tts.Voice
import grails.gorm.transactions.Transactional
import grails.web.api.ServletAttributes

@Transactional
class VideoService implements ServletAttributes {

    HistoriaService historiaService

    void createVideo(String videoName) {
        Video videoBase = new Video(ApplicationConfig.getVideoBasePath() + "/" + videoName)

        if (!videoBase.fileAlreadyExists()) {
            throw new Exception("Sem arquivo de video para uso.")
        }

        Integer tamanhoVideoBase = videoBase.duracao
        if (!tamanhoVideoBase) {
            throw new Exception("Video sem tempo para uso.")
        }

        if (!videoBase.isVertical()) {
            videoBase.crop()
        }
        if (videoBase.hasSound()) {
            videoBase.removeSound()
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

        Video video = new Video(path + "/video.mp4")
        videoBase.cut(0, tamanhoFinal.toInteger(), video.path)

        video.setAudio(audioFinal)
        video.addAudio()

        videoBase.cut(tamanhoFinal.toInteger(), tamanhoVideoBase)

        Image image = new Image(path + '/image.png', historia)
        image.createImage()

        BigDecimal tempoTitulo = titulo.duracao + swipe.duracao

        video.addImage(image, tempoTitulo)
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

    List<String> segmentVideo(Video video, Integer tempoVideo, Long idHistoria) {
        List<Integer> tempoPorVideo = getSegmentVideoSize(tempoVideo)

        List<String> videos = []
        Integer tempoInicial = 0
        tempoPorVideo.eachWithIndex{ Integer tempo, Integer i ->
            Video novoVideo = new Video(ApplicationConfig.getVideoBasePath() + "/historia_${idHistoria}/${i+1}_video.mp4")
            video.cut(tempoInicial, tempo, novoVideo.path)
            videos.add(novoVideo.path)
            tempoInicial += tempo - 1
        }

        new File(video.path).delete()

        return videos
    }

    String getSessionId() {
        return params.sessionId
    }

}
