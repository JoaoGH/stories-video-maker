package br.com.dark.svm.helper

class DirectoryHelper {

    static void deletarHistoria(String path) {
        new File(path).delete()
    }

}
