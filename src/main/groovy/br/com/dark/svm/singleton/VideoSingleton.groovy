package br.com.dark.svm.singleton

import br.com.dark.svm.ApplicationConfig
import br.com.dark.svm.enums.BackgroundVideoEnum
import br.com.dark.svm.helper.DirectoryHelper
import br.com.dark.svm.media.Video

class VideoSingleton {

    private static VideoSingleton instance = null
    private Map<BackgroundVideoEnum, List<Video>> videosBase = [:]

    static VideoSingleton getInstance() {
        if (instance == null) {
            instance = new VideoSingleton()
            String base = ApplicationConfig.getVideoBasePath()
            for (BackgroundVideoEnum it : BackgroundVideoEnum.values()) {
                String diretorio = "$base/base_videos/${it.folder}".toString()
                if (!DirectoryHelper.folderExists(diretorio)) {
                    DirectoryHelper.createFolder(diretorio)
                }
                List<File> arquivosDeVideo = DirectoryHelper.getFilesFromDirectory(diretorio)
                List<Video> videos = []
                for (File videoBase : arquivosDeVideo) {
                    Video video = new Video(videoBase.absolutePath)
                    if (video.hasSound()) {
                        video.removeSound()
                    }
                    if (!video.isVertical()) {
                        video.crop()
                    }
                    if (video.duracao > ApplicationConfig.getLimitForBaseVideo() + 1) {
                        List<Video> abc = splitVideo(video)
                        videos.addAll(abc)
                    }
                    videos.add(video)
                }
                instance.videosBase[it] = videos.sort { Video v -> v.duracao }
            }
        }

        return instance
    }

    List<Video> getAllVideos() {
        return videosBase.values().flatten() as List<Video>
    }

    Video getNextVideo(BackgroundVideoEnum tipoVideo, BigDecimal tempoNecessario) {
        for (Video it : videosBase.get(tipoVideo)) {
            if (it.duracao >= tempoNecessario) {
                return it
            }
        }
        return null
    }

    protected static List<Video> splitVideo(Video base) {
        List<Video> novos = []
        Integer limite = ApplicationConfig.getLimitForBaseVideo()

        while (base.duracao > limite) {
            Video novoVideo = new Video(base.directory + "/" + UUID.randomUUID().toString() + ".mp4")
            base.cut(0, limite, novoVideo.getPath())
            base.cut(limite, base.duracao.toInteger())
            novoVideo.updateDuracao()
            novos.add(novoVideo)
        }

        return novos
    }

    Boolean hasVideos() {
        return videosBase && videosBase.values().flatten().size() > 0
    }

}
