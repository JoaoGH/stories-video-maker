package br.com.dark.svm.helper

import java.nio.file.Files
import java.nio.file.Path

class DirectoryHelper {

    static void deletarHistoria(String path) {
        new File(path).delete()
    }

    static Boolean fileExists(String path) {
        return Files.exists(Path.of(path))
    }

    static String getDirectoryFromPath(String path) {
        return new File(path.find(/.*(?<=\/)/)).absolutePath
    }

}
