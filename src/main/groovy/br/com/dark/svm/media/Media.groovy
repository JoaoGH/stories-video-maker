package br.com.dark.svm.media

import br.com.dark.svm.helper.DirectoryHelper
import groovy.util.logging.Slf4j

@Slf4j
abstract class Media {

    String path
    BigDecimal duracao

    abstract protected BigDecimal getTempoDuracao()

    Boolean fileAlreadyExists() {
        return DirectoryHelper.fileExists(path)
    }

    String getDirectory() {
        return DirectoryHelper.getDirectoryFromPath(path)
    }

    String getFileName() {
        return path.find(/([^\/]+$)/)
    }

    void delete() {
        new File(path).delete()
    }

    /**
     * Uma Closure que realiza a execução de uma string. <br/>
     * Pode receber uma string ou uma lista de strings (para argumentos com espaços). <br/>
     * Imprime toda a saída, avisa para em caso de erro.
     *
     * */
    protected Closure runCommand = { strList ->
        assert (strList instanceof String || (strList instanceof List && strList.each({ it instanceof String })))
        Process proc = strList.execute()

        StringBuilder output = new StringBuilder()
        StringBuilder error = new StringBuilder()

        proc.inputStream.eachLine { String line ->
            log.debug(line)
            output.append(line).append('\n')
        }
        proc.errorStream.eachLine { String line ->
            log.debug(line)
            error.append(line).append('\n')
        }

        proc.out.close()
        proc.waitFor()

        if (strList instanceof List) {
            log.info((strList as List).join(" "))
        } else {
            log.info(strList.toString())
        }

        if (proc.exitValue()) {
            log.error("gave the following error: ${error.toString()}")
        }

        assert !proc.exitValue()

        return output.toString().trim()
    }

}
