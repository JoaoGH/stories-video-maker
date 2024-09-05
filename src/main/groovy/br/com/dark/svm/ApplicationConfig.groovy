package br.com.dark.svm

import br.com.dark.svm.media.Audio
import br.com.dark.svm.media.Image
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

    static Image getRedditBaseImagePath() {
        Image redditImage = new Image(getConfig().getProperty("video.image.reddit", String))
        if (redditImage.fileAlreadyExists()) {
            return redditImage
        }
        return new Image("./src/main/resources/reddit_base.png")
    }

    static Audio getSwipe() {
        Audio swipe = new Audio(getConfig().getProperty('video.audio.swipe', String))
        if (swipe.fileAlreadyExists()) {
            return swipe
        }
        return new Audio('./src/main/resources/swipe.png')
    }

    static Audio getLastSwipe() {
        Audio lastSwipe = new Audio(getConfig().getProperty('video.audio.last-swipe', String))
        if (lastSwipe.fileAlreadyExists()) {
            return lastSwipe
        }
        return new Audio('./src/main/resources/last_swipe.mp3')
    }

    static Integer getLimitSizeShort() {
        return 55
    }

    static Integer getLimitForBaseVideo() {
        return getConfig().getProperty("video.base.limit-time", Integer, 600)
    }

}
