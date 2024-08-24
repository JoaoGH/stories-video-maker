package br.com.dark.svm.media

import br.com.dark.svm.exception.InvalidVideo

import java.math.RoundingMode

class Video extends Media {

    Audio audio

    Video(String path) {
        this.path = path
        if (fileAlreadyExists()) {
            this.duracao = getTempoDuracao()
        }
    }

    @Override
    protected BigDecimal getTempoDuracao() {
        StringBuilder command = new StringBuilder()
        command << "ffprobe "
        command << " -v error"
        command << " -select_streams v:0"
        command << " -show_entries stream=duration"
        command << " -of default=noprint_wrappers=1:nokey=1 ${path}"

        String retornoComando = runCommand(command.toString())

        if (!retornoComando) {
            throw new InvalidVideo("Não foi possível identificar o tamanho do vídeo '${path}'.")
        }

        BigDecimal retorno = (retornoComando as BigDecimal)

        if (!retorno) {
            throw new InvalidVideo("Não foi possível identificar o tamanho do vídeo '${path}'.")
        }

        return retorno
    }

    void crop() {
        int vertical = 656
        String outputFile = directory + '/temp-' + path.find(/([^\/]+$)/)
        StringBuilder command = new StringBuilder()
        command.append("HandBrakeCLI")
        command.append(" -i ${path}")
        command.append(" -o ${outputFile}")
        command.append(" --crop 0:0:${vertical}:${vertical}")
        runCommand(command.toString())
        new File(path).delete()
        new File(outputFile).renameTo(path)
    }

    boolean isVertical() {
        Boolean vertical = false
        StringBuilder command = new StringBuilder()
        command.append("ffprobe ")
        command.append(" -v error")
        command.append(" -select_streams v:0")
        command.append(" -show_entries stream=width,height -of csv=s=x:p=0")
        command.append(" ${path}")
        String output = runCommand(command.toString())
        output.find(/(\d+)x(\d+)/) { String match, String w, String h ->
            Integer width = new BigDecimal(w).toInteger()
            Integer height = new BigDecimal(h).toInteger()
            vertical = height > width
        }
        return vertical
    }

    void removeSound() {
        String outputFile = directory + '/temp-' + path.find(/([^\/]+$)/)
        StringBuilder command = new StringBuilder()
        command.append("ffmpeg")
        command.append(" -i ${path}")
        command.append(" -an -c:v copy ${outputFile}")
        runCommand(command.toString())
        new File(path).delete()
        new File(outputFile).renameTo(path)
    }

    Boolean hasSound() {
        StringBuilder command = new StringBuilder()
        command.append("ffprobe")
        command.append(" -v error")
        command.append(" -select_streams a")
        command.append(" -show_entries stream=index")
        command.append(" -of csv=p=0 ${path}")
        String output = runCommand(command.toString())
        Boolean hasAudio = !output.isEmpty()
        return hasAudio
    }

    void cut(Integer tempoInicial, Integer tempoFinal, String output = null) {
        String outputFile = output ?: directory + '/temp-' + path.find(/([^\/]+$)/)
        StringBuilder command = new StringBuilder()
        command.append("HandBrakeCLI")
        command.append(" -i ${path}")
        command.append(" -o ${outputFile}")
        command.append(" --start-at duration:${tempoInicial}")
        command.append(" --stop-at duration:${tempoFinal}")
        runCommand(command.toString())
        if (!output) {
            new File(path).delete()
            new File(outputFile).renameTo(path)
        }
    }

    void addAudio() {
        String outputFile = directory + '/temp-' + path.find(/([^\/]+$)/)
        StringBuilder command = new StringBuilder()
        command.append("ffmpeg")
        command.append(" -i ${path}")
        command.append(" -i ${audio.path}")
        command.append(" -c:v copy -c:a aac")
        command.append(" -strict experimental ${outputFile}")
        runCommand(command.toString())
        new File(path).delete()
        new File(outputFile).renameTo(path)
    }

    void addImage(Image image, BigDecimal duracao) {
        String outputFile = directory + '/temp-' + path.find(/([^\/]+$)/)

        StringBuilder command = new StringBuilder()
        command.append("python3 src/main/python/br/com/dark/svm/add_image.py")
        command.append(" ${path}")
        command.append(" ${image.path}")
        command.append(" ${duracao.setScale(2, RoundingMode.CEILING)}")
        command.append(" ${outputFile}")

        runCommand(command.toString())

        new File(path).delete()
        new File(outputFile).renameTo(path)
    }

}
