package br.com.dark.svm.media

import br.com.dark.svm.ApplicationConfig
import groovy.util.logging.Slf4j

@Slf4j
class Shorts extends Media {

    Video video
    Audio titulo
    Audio conteudo
    List<Video> videosCurtos

    private final static Integer TEMPO_MAXIMO_SHORTS = 45

    Shorts(Video video, Audio titulo, Audio conteudo) {
        this.path = video.directory + '/temp.mp4'
        this.video = video
        this.titulo = titulo
        this.conteudo = conteudo
        this.duracao = getTempoDuracao()
        this.videosCurtos = []
    }

    @Override
    protected BigDecimal getTempoDuracao() {
        BigDecimal tempo = BigDecimal.ZERO
        tempo += ApplicationConfig.getSwipe().duracao
        tempo += titulo.duracao
        tempo += ApplicationConfig.getSwipe().duracao
        tempo += conteudo.duracao
        tempo += ApplicationConfig.getLastSwipe().duracao
        return tempo
    }

    /**
     * Cria uma lista com a duração que cada vídeo deve ter
     *
     * */
    protected List<Integer> getSegmentVideoSize(BigDecimal tempo = duracao) {
        List<Integer> segments = []

        if (tempo <= TEMPO_MAXIMO_SHORTS) {
            return [tempo]
        }

        // Calcula o número ideal de segmentos necessários para obter segmentos semelhantes
        Integer totalSegmentosVideo = (Integer) Math.ceil((double) tempo / TEMPO_MAXIMO_SHORTS)

        // Calcula o tempo base de cada segmento
        Integer tempoBaseSegmento = (Integer) (tempo / totalSegmentosVideo)

        // Calcula o tempo restante a ser distribuído
        Integer tempoRestante = tempo.toInteger() % totalSegmentosVideo

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

    /**
     * Criar arquivos de video shorts
     * */
    void makeShortsByVideo() {
        List<Integer> listaTempo = getSegmentVideoSize()

        log.info("Serão criados ${listaTempo.size()} arquivo(s) de shorts, duração média de ${listaTempo.average()} segundos")

        Integer tempoInicial = 0
        listaTempo.eachWithIndex { Integer tempo, Integer i ->
            Video videoCurto = new Video(directory + "/${i + 1}_" + video.getFileName())
            Integer tempoFinal = listaTempo.subList(0, i + 1).sum() as Integer
            video.cut(tempoInicial, tempoFinal, videoCurto.path)
            videosCurtos.add(videoCurto)
            log.info("Arquivo '${videoCurto.path}' criado com sucesso (${i+1}/${listaTempo.size()}).")
            tempoInicial += tempo
            if (i == 0) {
                tempoInicial -= 2
            }
        }
    }


//    /**
//     * Retorna uma lista de Vídeos segmentados.
//     * */
//    List<Video> segmentVideo() {
//        List<Integer> tempoPorVideo = getSegmentVideoSize()
//        Long idHistoria = titulo.historia.id
//
//        List<Video> videos = []
//        Integer tempoInicial = 0
//        tempoPorVideo.eachWithIndex { Integer tempo, Integer i ->
//            String pathNovoVideo = ApplicationConfig.getVideoBasePath() + "/historia_${idHistoria}/${i+1}_video.mp4"
//            Video novoVideo = new Video(pathNovoVideo)
//            video.cut(tempoInicial, tempo, novoVideo.path)
//            videos.add(novoVideo)
//            tempoInicial += tempo - 1
//        }
//
//        return videos
//    }
//
//    void addAudiosIntoVideo(List<Audio> audios) {
//        BigDecimal duracaoFinal = 0
//        Audio titulo = audios.pop()
//        audios.eachWithIndex { Audio audio, int i ->
//            String v = directory + "/${i + 1}_video.mp4"
//            StringBuilder command = new StringBuilder()
//            command.append("ffmpeg")
//            command.append(" -i ${video}")
//            command.append(" -i ${audio}")
//            command.append(" -c:v copy -c:a aac")
//            command.append(" -strict experimental ${v}")
//            duracaoFinal += titulo.duracao + audio.duracao
//            println ''
////            runCommand(command.toString())
//        }
//        println ''
////        new File(video).delete()
////        new File(temp).renameTo(video)
//    }
//
//    List<Audio> criarNovosAudios() {
//        List<Audio> audios = []
//
//        Audio swipe = new Audio(ApplicationConfig.getVideoBasePath() + "/swipe.mp3")
//        Audio novoTitulo = new Audio("${directory}/novo_titulo.mp3")
//
//        List<Integer> segmentosAudioConteudo = getSegmentVideoSize(conteudo.duracao)
//
//        novoTitulo.concat([swipe, titulo, swipe])
//        audios.add(novoTitulo)
////        new File(titulo.path).delete()
////        new File(novoTitulo.path).renameTo(titulo.path)
//
//        Integer tempoInicial = 0
//        segmentosAudioConteudo.eachWithIndex { Integer tempo, Integer i ->
//            Audio audioConteudo = new Audio(directory + "/${i + 1}_conteudo.mp3")
//            StringBuilder command = new StringBuilder()
//            command.append("ffmpeg")
//            command.append(" -i ${conteudo}")
//            command.append(" -ss ${tempoInicial} -to ${segmentosAudioConteudo.subList(0, i + 1).sum()}")
//            command.append(" -c copy ${audioConteudo.path}")
//            runCommand(command.toString())
//            audios.add(audioConteudo)
//            tempoInicial += tempo
//            if (i == 0) {
//                tempoInicial -= 1
//            }
//        }
//
//        Audio pausa = new Audio(ApplicationConfig.getVideoBasePath() + "/pausa.mp3")
//        Audio ultimoConteudo = new Audio(directory + "/temp-" + audios.last().getFileName())
//
//        ultimoConteudo.concat([audios.last(), pausa])
//
////        new File(ultimoConteudo).delete()
////        new File(finalAudio).renameTo(ultimoConteudo)
//
//        return audios
//    }

}
