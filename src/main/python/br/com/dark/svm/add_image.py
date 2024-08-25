from moviepy.editor import VideoFileClip, ImageClip, CompositeVideoClip
from PIL import Image, ImageFilter
import sys


# Caminhos dos arquivos
resampling_filter = Image.Resampling.LANCZOS

def edit_video(video_path, image_path, time_audio, output_path):
    # Carregar o vídeo e a imagem
    video_clip = VideoFileClip(video_path)
    image_clip = ImageClip(image_path)

    image_clip = image_clip.set_duration(video_clip.duration)

    start_time = 0.1
    slide_image_clip = slideImage(image_clip, start_time, 0.1, time_audio)

    final_clip = CompositeVideoClip([video_clip, slide_image_clip], size=video_clip.size)

    # Exportar o vídeo final
    final_clip.write_videofile(output_path, codec="libx264")

def resizeImage(image, height):
    # Redimensionar a imagem manualmente usando Pillow
    try:
        with Image.open(image_path) as img:
            # Novo tamanho da imagem
            height = height // 4
            width = int(img.width * (height / img.height))

            # Redimensionar a imagem usando LANCZOS
            img_resized = img.resize((width, height), resampling_filter)
            img_resized.save("/home/joao/Pictures/resized_image.png")

            # Carregar a imagem redimensionada no MoviePy
            image = ImageClip("/home/joao/Pictures/resized_image.png").set_duration(video_clip.duration)
            ##print(f"Imagem redimensionada para: {image.size}")
    except Exception as e:
        print(f"Erro ao redimensionar a imagem: {e}")
        image = None

    return image

def slideImage(image_clip, start_time, slide_duration=0.1, static_duration=5, margin_top=250):
    """
    Aplica um efeito de slide up, mantem a imagem estática e depois aplica um slide down.

    :param image_clip: O ImageClip a ser animado.
    :param start_time: Tempo de início da animação no vídeo.
    :param slide_duration: Duração da animação de slide up e slide down.
    :param static_duration: Tempo que a imagem deve ficar estática.
    :param margin_top: Margem do topo onde a imagem deve parar após o slide up.
    :return: ImageClip com os efeitos aplicados.
    """
    video_height = image_clip.h
    image_height = image_clip.h
    total_distance = video_height + image_height - margin_top

    def position(t):
        if t < start_time:
            return ('center', -image_height)  # Começa fora da tela
        elif t < start_time + slide_duration:
            # Slide up
            progress = (t - start_time) / slide_duration
            current_position = -total_distance + (total_distance * progress)
            return ('center', current_position)
        elif t < start_time + slide_duration + static_duration:
            # Fica estática
            return ('center', margin_top)
        elif t < start_time + slide_duration + static_duration + slide_duration:
            # Slide down
            progress = (t - (start_time + slide_duration + static_duration)) / slide_duration
            current_position = margin_top + (total_distance * progress)
            return ('center', current_position)
        else:
            return ('center', video_height)  # Termina fora da tela na parte inferior

    return image_clip.set_position(lambda t: position(t)).set_start(start_time).set_duration(
        slide_duration * 2 + static_duration)


video_path = sys.argv[1]
image_path = sys.argv[2]
time_audio = float(sys.argv[3])
output_path = sys.argv[4]

edit_video(video_path, image_path, time_audio, output_path)