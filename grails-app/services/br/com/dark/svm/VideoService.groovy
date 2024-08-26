package br.com.dark.svm

import br.com.dark.svm.command.VideoCommand
import br.com.dark.svm.enums.BackgroundVideoEnum
import br.com.dark.svm.enums.HistoriaOrigemEnum
import br.com.dark.svm.enums.HistoriaStatusEnum
import br.com.dark.svm.exception.InvalidVideoException
import br.com.dark.svm.helper.DirectoryHelper
import br.com.dark.svm.media.Audio
import br.com.dark.svm.media.Image
import br.com.dark.svm.media.Media
import br.com.dark.svm.media.Shorts
import br.com.dark.svm.media.Video
import br.com.dark.svm.tts.Voice
import grails.gorm.transactions.Transactional
import javassist.NotFoundException

import java.time.LocalDateTime

@Transactional
class VideoService {

    HistoriaService historiaService

    Map prepareScenario() {
        List<String> nomeVideos = BackgroundVideoEnum.values()*.videoName
        String base = ApplicationConfig.getVideoBasePath()
        List<Video> videos = nomeVideos.collect { String it -> new Video("${base}/${it}") }
        List<Audio> audios = [ApplicationConfig.getSwipe(), ApplicationConfig.getLastSwipe()]

        prepareScenario(videos, audios)
    }

    Map prepareScenario(List<Video> videos, List<Audio> audios) {
        Map retorno = [sucess: true, videos: [], audios: []]

        videos.each { Video video ->
            if (!video.isVertical()) {
                video.crop()
            }

            if (video.hasSound()) {
                video.removeSound()
            }

            retorno.videos << video.path
        }

        audios.each { Audio it ->
            it.encode()
            retorno.audios << it.path
        }

        return retorno
    }

    Map createVideo(VideoCommand command) {
        Map retorno = [success: true]

        BackgroundVideoEnum backgroundVideo = BackgroundVideoEnum.value(command.background)
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + backgroundVideo.videoName

        if (command.id) {
            Historia historia = historiaService.get(command.id)
            return createVideo(historia, videoBase, command.sessionId, command.shorts)
        }

        List<Historia> historias = historiaService.list([status: HistoriaStatusEnum.OBTIDA.getValue()])

        retorno.data = []
        for (Historia historia : historias) {
            try {
                Map video = createVideo(historia, videoBase, command.sessionId, command.shorts)
                retorno.data << video
            } catch (Exception e) {
                log.error("Erro ao criar video para ${historia.toString()}. Passando para próxima execução.", e)
                retorno.data << [
                        success: false,
                        message: "Erro ao criar video para ${historia.toString()}."
                ]
                DirectoryHelper.deletarHistoria(ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}")
            }
        }

        return retorno
    }

    Map createVideo(Historia historia, String videoBasePath, String sessionId, Boolean makeShorts) {
        Map retorno = [success: true]
        String path = ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}"

        if (DirectoryHelper.folderExists(path)) {
            throw new Exception("Pasta '$path' já criada, logo a produção da ${historia.toString()} já foi inicializada.")
        }

        DirectoryHelper.createFolder(path)

        Video videoBase = new Video(videoBasePath)

        if (!videoBase.fileAlreadyExists()) {
            throw new InvalidVideoException("Sem arquivo de video para uso.")
        }

        BigDecimal tamanhoVideoBase = videoBase.duracao
        if (!tamanhoVideoBase) {
            throw new InvalidVideoException("Video sem tempo para uso.")
        }

        Audio swipe = ApplicationConfig.getSwipe()
        Audio lastSwipe = ApplicationConfig.getLastSwipe()

        prepareScenario([videoBase], [swipe, lastSwipe])

        Audio titulo = new Audio(path + "/titulo.mp3", historia.titulo)
        titulo.setVoz(Voice.PORTUGUESE_BR_MALE)
        titulo.createAudioFileTTS(sessionId)

        Audio conteudo = new Audio(path + "/conteudo.mp3", historia.conteudo)
        conteudo.setVoz(Voice.PORTUGUESE_BR_MALE)
        conteudo.createAudioFileTTS(sessionId)

        Audio audioFinal = new Audio(path + "/audio_final.mp3")
        audioFinal.concat([swipe, titulo, swipe, conteudo, lastSwipe])

        BigDecimal tamanhoFinal = audioFinal.getDuracao()

        if (tamanhoVideoBase < tamanhoFinal) {
            retorno.success = false
            retorno.message = "Video base com ${formatTime(tamanhoVideoBase.toInteger())} insuficiente para ${formatTime(tamanhoFinal.toInteger())}"
            DirectoryHelper.deletarHistoria(path)
            return retorno
        }

        Video video = new Video(path + "/video.mp4")
        videoBase.cut(0, tamanhoFinal.toInteger(), video.path)

        video.setAudio(audioFinal)
        video.addAudio()

        videoBase.cut(tamanhoFinal.toInteger(), tamanhoVideoBase.toInteger())

        Image image = new Image(path + '/image.png', historia)
        image.createImage()

        BigDecimal tempoTitulo = titulo.duracao + swipe.duracao

        video.addImage(image, tempoTitulo)

        log.info("Atualizar ${historia.toString()}.")
        Map novosValores = [
                status         : !makeShorts ? HistoriaStatusEnum.CRIADA.getValue() : historia.status,
                dataHoraCriacao: LocalDateTime.now()
        ]
        historiaService.update(novosValores, historia)

        log.info("Criação do video ${historia.toString()} finalizado.")

        retorno.historia = historia.toString()
        retorno.path = video.path

        if (!makeShorts) {
            log.info("Remover arquivos restantes.")
            removeFiles([titulo, conteudo, audioFinal, image])
            return retorno
        }

        retorno.message = "Necessário adicionar legendas em 'https://www.capcut.com/' antes de criar os shorts."

        return retorno
    }

    void removeFiles(List<Media> arquivosExtras) {
        arquivosExtras*.delete()
    }

    protected String formatTime(Integer totalSeconds) {
        Integer hours = (Integer) (totalSeconds / 3600);
        Integer minutes = (Integer) ((totalSeconds % 3600) / 60);
        Integer seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    Map makeShorts(Long id) {
        Map retorno = [success: true]

        Historia historia = historiaService.get(id)

        if (!historia) {
            throw new NotFoundException("Historia com ID ${id} não encontrada.")
        }

        log.info("Identificar arquivos necessários para criar os shorts.")

        String path = ApplicationConfig.getVideoBasePath() + "/historia_${historia.id}"
        Video video = new Video(path + "/video.mp4")
        Audio titulo = new Audio(path + "/titulo.mp3", historia.titulo)
        Audio conteudo = new Audio(path + "/conteudo.mp3", historia.conteudo)
        Audio audioFinal = new Audio(path + "/audio_final.mp3")
        Image image = new Image(path + '/image.png', historia)

        List<Media> medias = [video, titulo, conteudo, audioFinal, image]

        medias.each { Media it ->
            if (!it.fileAlreadyExists()) {
                throw new NotFoundException("Falha ao buscar arquivo ${it.path}.")
            }
        }

        log.info("Arquivos identificados, inicializando criação de shorts.")

        Shorts shorts = new Shorts(video, titulo, conteudo)
        shorts.makeShortsByVideo()

        log.info("Videos curtos criados com sucesso.")

        medias.pop()
        removeFiles(medias)

        retorno.videos = shorts.videosCurtos.path
        retorno.arquivosRemovidos = medias.path

        log.info("Atualizar ${historia.toString()}.")
        Map novosValores = [
                status         : HistoriaStatusEnum.CRIADA.getValue(),
                dataHoraCriacao: LocalDateTime.now()
        ]
        historiaService.update(novosValores, historia)

        return retorno
    }

}
