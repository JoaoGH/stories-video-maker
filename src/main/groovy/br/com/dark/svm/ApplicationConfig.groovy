package br.com.dark.svm

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

}
