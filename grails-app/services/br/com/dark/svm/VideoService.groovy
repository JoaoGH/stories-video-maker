package br.com.dark.svm

import grails.gorm.transactions.Transactional

import java.nio.file.Files
import java.nio.file.Path

@Transactional
class VideoService {

    void createVideo(String videoName) {
        String videoBase = ApplicationConfig.getVideoBasePath() + "/" + videoName

        if (!Files.exists(Path.of(videoBase))) {
            throw new Exception("Sem arquivo de video para uso.")
        }

    }

}
