package br.com.dark.svm.media

import br.com.dark.svm.ApplicationConfig
import br.com.dark.svm.Historia
import groovy.util.logging.Slf4j

@Slf4j
class Image extends Media {

    Historia historia

    Image(String path, Historia historia) {
        this.path = path
        this.historia = historia
    }

    @Override
    protected BigDecimal getTempoDuracao() {
        return null
    }

    void createImage() {
        String titulo = historia.titulo.bytes.encodeBase64().toString()

        log.info("Criar imagem '${path}'.")
        StringBuilder imagem = new StringBuilder()
        imagem.append("python3 src/main/python/br/com/dark/svm/create_image.py")
        imagem.append(" ${ApplicationConfig.getRedditBaseImagePath()}")
        imagem.append(" ${path}")
        imagem.append(" ${titulo}")
        imagem.append(" /usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")
        imagem.append(" 20")

        runCommand(imagem.toString())
    }

}
