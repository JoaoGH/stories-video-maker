package br.com.dark.svm.media

import br.com.dark.svm.Historia
import br.com.dark.svm.exception.InvalidAudioException
import br.com.dark.svm.helper.DirectoryHelper
import br.com.dark.svm.tts.TiktokTTS
import br.com.dark.svm.tts.Voice
import groovy.util.logging.Slf4j
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.JavaLayerException

import java.nio.file.Files
import java.nio.file.Paths

@Slf4j
class Audio extends Media {

    String conteudo
    Voice voz
    Historia historia

    Audio(String path) {
        this.path = path
        if (fileAlreadyExists()) {
            this.duracao = getTempoDuracao()
        }
    }

    Audio(String path, String conteudo) {
        this.path = path
        this.conteudo = conteudo
        if (fileAlreadyExists()) {
            this.duracao = getTempoDuracao()
        }
    }

    void createAudioFileTTS(String sessionId) {
        File file = Paths.get(path).toFile()
        TiktokTTS tts = new TiktokTTS(sessionId, voz, conteudo, file)
        log.info("Criar arquivo de audio '${path}'.")
        try {
            tts.createAudioFile()
        } catch (Exception e) {
            log.error("Erro ao criar arquivo de áudio para '${historia.toString()}'. Pasta será deletada.")
            DirectoryHelper.deletarHistoria(directory)
            throw e
        }
        this.duracao = getTempoDuracao()
    }

    @Override
    protected BigDecimal getTempoDuracao() {
        BigDecimal durationInSeconds = BigDecimal.ZERO

        FileInputStream fileInputStream = new FileInputStream(path)
        Bitstream bitstream = new Bitstream(fileInputStream)
        Header header

        log.info("Obter duração do audio ${path}.")
        try {
            while ((header = bitstream.readFrame()) != null) {
                durationInSeconds += header.ms_per_frame() / 1000.0
                bitstream.closeFrame()
            }
        } catch (JavaLayerException e) {
            log.error("Erro ao calcular o tamanho final do audio '${path}'.")
            throw new InvalidAudioException(e)
        } finally {
            fileInputStream.close()
        }

        return durationInSeconds
    }

    void concat(List<Audio> audios) {
        FileOutputStream outputStream = new FileOutputStream(path)

        log.info("Concatenar arquivos de audio ${audios*.path.toString()}.")
        audios*.path.each { String mp3FilePath ->
            byte[] mp3Bytes = Files.readAllBytes(Paths.get(mp3FilePath))
            outputStream.write(mp3Bytes)
        }

        outputStream.close()

        this.duracao = getTempoDuracao()
    }

    void encode() {
        String tempFile = directory + '/temp-' + getFileName()

        StringBuilder command = new StringBuilder()
        command << "ffmpeg"
        command << " -i ${path}"
        command << " -ar 24000"
        command << " -ac 1"
        command << " -b:a 128k ${tempFile}"
        runCommand(command.toString())

        log.info("Deletar arquivo temporário '${tempFile}'.")
        new File(path).delete()
        new File(tempFile).renameTo(path)
    }

}
