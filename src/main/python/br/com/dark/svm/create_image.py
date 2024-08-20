# Importing the PIL library
from PIL import Image
from PIL import ImageDraw
from PIL import ImageFont
import textwrap
import sys
import base64

def criarImagem(imagem_base, imagem_final, titulo_historia, fonte, tamanho_fonte):
    max_width = 30  # Ajuste esse valor conforme necessário

    TEXT_BOX_MARGEN_Y = 36
    TEXT_BOX_MARGEN_x = 20

    # Carrega a imagem
    image = Image.open(imagem_base)
    draw = ImageDraw.Draw(image)

    # Define a fonte e o tamanho do texto
    font = ImageFont.truetype(fonte, tamanho_fonte)

    wrapped_text = textwrap.fill(titulo_historia, width=max_width)

    # Calcula a altura total das linhas de texto
    lines = wrapped_text.split('\n')
    line_height = draw.textbbox((0, 0), 'A', font=font)[3]  # Altura de uma linha de texto
    total_text_height = line_height * len(lines)

    # Centraliza a caixa de texto na imagem
    box_y = (image.height - total_text_height) // 2 + TEXT_BOX_MARGEN_Y

    # Desenha cada linha de texto
    for line in lines:
        line_bbox = draw.textbbox((0, 0), line, font=font)
        line_width = line_bbox[2] - line_bbox[0]
        text_x = (image.width - line_width) // 2 + TEXT_BOX_MARGEN_x
        draw.text((text_x, box_y), line, font=font, fill="black")
        box_y += line_height  # Move a posição Y para a próxima linha


    # Salva a imagem editada
    image.save(imagem_final)




caminho_imagem = sys.argv[1] ## "contador_reddit.png"
caminho_imagem_final = sys.argv[2] ## "image_2.png"
titulo = sys.argv[3] ## "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque scelerisque dignissim sapien, id vulputate leo fermentum auctor."
titulo = base64.b64decode(titulo).decode('utf-8')

fonte_caminho = sys.argv[4] ##"/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
fonte_tamanho = int(sys.argv[5]) ##20

criarImagem(caminho_imagem, caminho_imagem_final, titulo, fonte_caminho, fonte_tamanho)
