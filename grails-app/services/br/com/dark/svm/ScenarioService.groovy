package br.com.dark.svm

import br.com.dark.svm.media.Audio
import br.com.dark.svm.media.Video
import br.com.dark.svm.singleton.VideoSingleton
import grails.gorm.transactions.Transactional

@Transactional
class ScenarioService {

    VideoSingleton videoSingleton = VideoSingleton.getInstance()

    Map prepareScenario() {
        List<Video> videos = videoSingleton.getAllVideos()
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

}
