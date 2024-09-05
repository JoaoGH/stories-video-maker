# Stories Video Maker

API destina a criar videos curtos de leitura de historias usando voz de IA.

> JDK 11\
> Grails 5.2.2\
> Python3 

## Instalação

Para que o sistema funcione corretamente é necessário realizar as configurações descritas abaixo.

### Dependências Python3

São usadas as seguintes dependências em python:
> moviepy@1.0.3\
> Pillow@10.4.0\
> textwrap\
> sys\
> base64

### Configuração externalizada
Para realizar a configuração externalizada basta criar um arquivo com o nome `application.yml` em uma das seguintes 
opções:
- ***home do usuário***/.grails/stories-video-maker-config/application.yml
- ***diretório base de configurações***/stories-video-maker-config/application.yml

### Configuração

Configurações que devem ser adicionadas para que a aplicação funcione corretamente.

```yml  
video.base.path: recebe uma string para indicar a pasta onde os videos base estarão e os finais serão criados.
video.base.limit-time: recebe um inteiro para indicar o tempo máximo (em segundos) para os vídeos base.
video.image.reddit: recebe uma string para indicar o caminho absoluto da imagem do reddit.
video.audio.swipe: recebe uma string para indicar o caminho absoluto do audio do swipe.
video.audio.last-swipe: recebe uma string para indicar o caminho absoluto do audio do ultimo swipe.
```  

#### video.base.path
Esse será o diretório base de toda a aplicação, será necessário adicionar os arquivos dos vídeos base nesse diretório,
dentro da pasta `base-videos` dentro do tipo definido do vídeo.<br>
Exemplo: `/home/user/Videos/stories-video-maker/base-videos/minecraft/`.

#### video.base.limit-time
Esse será o tamanho máximo para os vídeos base, isso para otimizar o tempo de manipulação, na hora de criar uma história
nova, se for passado um vídeo maior que o tempo definido, quebrará esse vídeo em vídeos menores.<br>
Caso não seja definido valor, usará por padrão 600 segundos (10 minutos).<br>
Exemplo: `123`.

#### video.image.reddit
Caminho absoluto para a imagem base usada para histórias do reddit.<br>
Caso não seja definido um caminho, ou esse seja inválido, usará [reddit_base.png](./src/main/resources/reddit_base.png)
presente no projeto.<br>
Exemplo: `/home/user/Picture/reddit_base.png`.

#### video.audio.swipe
Caminho absoluto para o áudio de "swipe" que será usado como efeito ao aparecer a imagem de título no vídeo.<br>
Caso não seja definido um caminho, ou esse seja inválido, usará [swipe.mp3](./src/main/resources/swipe.mp3) presente no 
projeto.<br>
Exemplo: `/home/user/Music/swipe_inicial.mp3`.

#### video.audio.last-swipe
Caminho absoluto para o áudio de "swipe" que será usado como efeito no final do vídeo, indicando o fim da história.<br>
Caso não seja definido um caminho, ou esse seja inválido, usará [last-swipe.mp3](./src/main/resources/last_swipe.mp3) 
presente no projeto.<br>
Exemplo: `/home/user/Music/swipe_final.mp3`.

## Funcionamento

O sistema funciona de maneira autonoma, sendo necessário usar alguma interface para realizar as request ao projeto, algo
como o [Postman](https://www.postman.com/), [Insomnia](https://insomnia.rest/), [cURL](https://curl.se/), etc...

O Stories Video Maker consiste em uma API que cria vídeos de leitura de histórias, as histórias são obtidas via request
aos fóruns alvos, a leitura é feita por uma funcionalidade TTS do Tiktok, o vídeo de fundo é fornecido pelo usuário da
API e a legenda é feita manualmente pelo [CapCut](https://www.capcut.com/pt-br/).

### Preparação de Cenário

Ao subir a aplicação, a mesma realiza uma série de validações e manipulações.
* Cria a pasta necessária para os arquivos de vídeos base, incluindo subpasta para cada tipo de vídeo base;
* Seta tamanho máximo nos vídeos base referente a ao retorno do método
[ApplicationConfig.getLimitForBaseVideo](./src/main/groovy/br/com/dark/svm/ApplicationConfig.groovy).
  * Fixamente definido em 600 segundos (10 minutos).
  * Isso é para otimizar a manipulação dos vídeos e reduzir o tempo de criação.
* Popula Singleton com os vídeos, separados por tipos de vídeo base.
* Realiza o encode dos arquivos de audios (swipe e last-swipe) no formato 128k.

### Criar vídeos

O processo para execução/criação de uma história se da pela requisição POST feita no endpoint `/video/create`, são 
necessários os seguintes parâmetros:

```json lines
{
  id: null, // inteiro identificador da tabela HISTORIA,
  sessionId: 'sessionIdTiktok', // sessionId de uma conta tiktok, pode ser obtida nos cookies do site com o nome de "sessionid"
  origem: '', // tipo de vídeo a ser criado, fórum de origem. 
  shorts: true, // se deve ser criado shorts (com limitador de tempo, segmentando em parte 1, 2...) ou apenas o vídeo completo.
  background: '', // Sigla do vídeo de background.
}
```

* Caso não seja enviado o id do vídeo será criado para todas as histórias cadastradas no banco, que já não tenham iniciado
sua criação.
* Não ainda uma criação de legendas automáticas, para faze-las deve-se usar o [CapCut](https://www.capcut.com/pt-br/).


### Criação de Histórias
Para que seja criado um vídeo de uma história, é necessário realizar o cadastro delas:

* Eu sou o Babaca? - request deve ser POST para `/reddit` - Realiza a busca no subreddit e salva no banco de dados.
* EM BREVE

A execução de um relatório se da quando é recebida uma requisição no endpoint `/v1/reportExecutor` informando o `id` do
registro da `in4gr_agendamentos`, com isso o Executor põe em usa fila interna a execução do relatório e atualiza o
status do registro de banco para GERANDO.

### Listagem de Histórias

Para listar as histórias presentes no banco de dados, é necessário realizar uma request GET para o endpoint de criação
da mesma, como no tópico acima. Exemplo:

* Eu sou o Babaca? - request deve ser GET para `/reddit` - Lista todas as histórias cadastradas no banco de dados.

* Filtros - EM BREVE


### Adicionar Legendas

Atualmente é necessário adicioná-las manualmente pelo [CapCut](https://www.capcut.com/pt-br/).

Geradas pela API **EM BREVE**.

### Quebrar em Shorts

Como é necessário criar o vídeo base com legenda primeiro, para criar os shorts, deve passar pelo passo
[Adicionar Legendas](#adicionar-legendas).

Para quebrar um vídeo em Shorts é necessário fazer uma request PUT para o endpoint `/video/makeShorts`, com os seguintes
parâmetros:
```json lines
{
  id: null // inteiro identificador da tabela HISTORIA
}
```

### Tipos de vídeos

Atualmente podem ser criados os seguintes tipos de vídeos:

* "AITA" - Eu sou o bababca (PT-BR).

### Vídeos de fundo aceitos

Atualmente podem ser criados vídeo com o background de:

* "M" -> Minecraft

## Log

Salvas informações das ações tomadas no sistema, como, por exemplo, a criação de um vídeo curto.

