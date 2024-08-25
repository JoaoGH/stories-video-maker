package br.com.dark.svm

import br.com.dark.svm.media.Audio
import grails.config.Config
import grails.util.Holders

class ApplicationConfig {

    static Config getConfig() {
        return Holders.getConfig()
    }

    static String getRedditAITMUrl() {
        return "https://www.reddit.com/r/EuSouOBabaca.json"
    }

    static String getVideoBasePath() {
        return getConfig().getProperty("video.base.path", String)
    }

    static String getRedditBaseImagePath() {
        return getConfig().getProperty("video.image.reddit", String)
    }

    static Audio getSwipe() {
        new Audio(getVideoBasePath() + '/swipe.mp3')
    }

    static Audio getLastSwipe() {
        new Audio(getVideoBasePath() + '/pausa.mp3')
    }

}
