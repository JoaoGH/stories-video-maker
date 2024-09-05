package br.com.dark.svm.helper

import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Path

class DirectoryHelper {

    static void deletarHistoria(String path) {
        FileUtils.deleteDirectory(new File(path))
    }

    static Boolean fileExists(String path) {
        return Files.exists(Path.of(path))
    }

    static Boolean folderExists(String dir) {
        File folder = new File(dir)
        return folder.exists() && folder.isDirectory()
    }

    static void createFolder(String path) {
        new File(path).mkdir()
    }

    static String getDirectoryFromPath(String path) {
        return new File(path.find(/.*(?<=\/)/)).absolutePath
    }

    static List<File> getFilesFromDirectory(String directory) {
        File dir = new File(directory)
        List<File> files = dir.listFiles().findAll { file ->
            file.isFile()
        }
        return files
    }

}
