package br.com.dark.svm.media

import br.com.dark.svm.tts.TiktokTTS
import br.com.dark.svm.tts.Voice
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.JavaLayerException

import java.nio.file.Files
import java.nio.file.Paths

class Audio extends Media {

    String conteudo
    Voice voz

    Audio(String path) {
        this.path = path
        if (fileAlreadyExists()) {
            this.duracao = getTempoDuracao()
        }
    }

    Audio(String path, String conteudo) {
        this.path = path
        this.conteudo = conteudo
    }

    void createAudioFileTTS(String sessionId) {
        File file = Paths.get(path).toFile()
        TiktokTTS ttsTitulo = new TiktokTTS(sessionId, voz, conteudo, file)
        ttsTitulo.createAudioFile()
        this.duracao = getTempoDuracao()
    }

    @Override
    protected BigDecimal getTempoDuracao() {
        BigDecimal durationInSeconds = BigDecimal.ZERO

        FileInputStream fileInputStream = new FileInputStream(path)
        Bitstream bitstream = new Bitstream(fileInputStream)
        Header header

        try {
            while ((header = bitstream.readFrame()) != null) {
                durationInSeconds += header.ms_per_frame() / 1000.0
                bitstream.closeFrame()
            }
        } catch (JavaLayerException e) {
            e.printStackTrace()
            throw new Exception("Erro ao calcular o tamanho final do audio '${path}'.")
        } finally {
            fileInputStream.close()
        }

        return durationInSeconds
    }

    void concat(List<Audio> audios) {
        FileOutputStream outputStream = new FileOutputStream(path)

        audios*.path.each { String mp3FilePath ->
            byte[] mp3Bytes = Files.readAllBytes(Paths.get(mp3FilePath))
            outputStream.write(mp3Bytes)
        }

        outputStream.close()

        this.duracao = getTempoDuracao()
    }

}