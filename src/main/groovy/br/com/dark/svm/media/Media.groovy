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
            println(line)
            output.append(line).append('\n')
        }
        proc.errorStream.eachLine { String line ->
            println(line)
            error.append(line).append('\n')
        }

        proc.out.close()
        proc.waitFor()

        print "[INFO] ( "
        if (strList instanceof List) {
            strList.each { print "${it} " }
        } else {
            print strList
        }
        println " )"

        if (proc.exitValue()) {
            println "gave the following error: "
            println "[ERROR] ${error.toString()}"
        }
        assert !proc.exitValue()

        return output.toString().trim()
    }

}
