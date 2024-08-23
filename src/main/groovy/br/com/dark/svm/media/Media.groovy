package br.com.dark.svm.media

import java.nio.file.Files
import java.nio.file.Path

abstract class Media {

    String path
    BigDecimal duracao

    abstract protected BigDecimal getTempoDuracao()

    Boolean fileAlreadyExists() {
        return Files.exists(Path.of(path))
    }

    String getDirectory() {
        return new File(path.find(/.*(?<=\/)/)).absolutePath
    }

}
