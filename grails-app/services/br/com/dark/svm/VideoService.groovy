package br.com.dark.svm

import br.com.dark.svm.command.VideoCommand
import br.com.dark.svm.enums.BackgroundVideoEnum
import br.com.dark.svm.enums.HistoriaOrigemEnum
import br.com.dark.svm.enums.HistoriaStatusEnum
import br.com.dark.svm.exception.InvalidVideoException
import br.com.dark.svm.helper.DirectoryHelper
import br.com.dark.svm.media.Audio
import br.com.dark.svm.media.Image
import br.com.dark.svm.media.Video
import br.com.dark.svm.tts.Voice
import grails.gorm.transactions.Transactional
import java.time.LocalDateTime

@Transactional
class VideoService {

    HistoriaService historiaService

    void createVideo(VideoCommand command) {
        BackgroundVideoEnum backgroundVideo = BackgroundVideoEnum.value(command.background)
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + backgroundVideo.videoName
        Historia historia
        if (command.id) {
            historia = historiaService.get(command.id)
        } else {
            historia = historiaService.getNextHistoria(HistoriaOrigemEnum.value(command.origem))
        }
        createVideo(historia, videoBase, command.sessionId, command.shorts)
    }

    void createVideo(Historia historia, String videoBasePath, String sessionId, Boolean makeShorts) {
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

        if (!videoBase.isVertical()) {
            videoBase.crop()
        }
        if (videoBase.hasSound()) {
            videoBase.removeSound()
        }

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

        videoBase.cut(tamanhoFinal.toInteger(), tamanhoVideoBase.toInteger())

        Image image = new Image(path + '/image.png', historia)
        image.createImage()

        BigDecimal tempoTitulo = titulo.duracao + swipe.duracao

        video.addImage(image, tempoTitulo)

        log.info("Atualizar ${historia.toString()}.")
        historiaService.update([status: HistoriaStatusEnum.CRIADA.getValue(), dataHoraCriacao: LocalDateTime.now()], historia)

        log.info("Criação do video ${historia.toString()} finalizado.")
    }

}
