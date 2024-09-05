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
            Integer tempoFinal = listaTempo.subList(0, i + 1).first()
            if (i > 0) {
                tempoFinal += 2
            }
            video.cut(tempoInicial, tempoFinal, videoCurto.path)
            videosCurtos.add(videoCurto)
            log.info("Arquivo '${videoCurto.path}' criado com sucesso (${i+1}/${listaTempo.size()}).")
            tempoInicial += tempo
            if (i == 0) {
                tempoInicial -= 2
            }
        }
    }

}
