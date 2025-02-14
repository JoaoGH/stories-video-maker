package br.com.dark.svm.tts

import br.com.dark.svm.exception.InvalidSessionIDException
import br.com.dark.svm.exception.TiktokTTSException
import br.com.dark.svm.request.Request
import br.com.dark.svm.request.ResponseContent

class TiktokTTS {

    private String sessionId;
    private Voice voice;
    private List<String> speeches = new ArrayList<>();
    private File output;
    private SpeechBreakMode breakMode;

    TiktokTTS(String sessionId, Voice voice, String speech, File output, SpeechBreakMode mode) {
        this.voice = voice;
        this.output = output;
        this.sessionId = sessionId;
        this.breakMode = mode;
        setSpeech(speech);
    }

    TiktokTTS(String sessionId, Voice voice, String speech, File output) {
        this(sessionId, voice, speech, output, SpeechBreakMode.BREAK_ON_PUNCTUATION);
    }

    public String getSessionId() {
        return sessionId;
    }

    public Voice getVoice() {
        return voice;
    }

    public List<String> getSpeeches() {
        return speeches;
    }

    public File getOutput() {
        return output;
    }

    public SpeechBreakMode getBreakMode() {
        return breakMode;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setVoice(Voice voice) {
        this.voice = voice;
    }

    void setSpeech(String speech) {
        speeches.clear();

        if (speech == null) {
            return
        }

        speeches.addAll(breakMode.getSpeechParts(speech));
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public void setBreakMode(SpeechBreakMode breakMode) {
        this.breakMode = breakMode;
    }

    private byte[] getAudioAsBytes(String speech) {
        String url = "https://api22-normal-c-useast1a.tiktokv.com/media/api/text/speech/invoke" +
                "/?text_speaker=" + voice.tiktokId + "&req_text=" + speech + "&speaker_map_type=0&aid=1233";

        Request request = new Request.Builder()
                .setUrl(url)
                .setHeaders([
                        "User-Agent": "com.zhiliaoapp.musically/2022600030 (Linux; U; Android 7.1.2; es_ES; SM-G988N; Build/NRD90M;tt-ok/3.12.13.1)",
                        "Cookie"    : "sessionid=" + sessionId
                ])
                .setMethod("POST")
                .build()

        ResponseContent responseContent = request.execute()

        Map<String, Object> map = responseContent.getBody()

        String message = map.get("message").toString()?.replaceAll('’', "'");

        if (message.equals("Couldn't load speech. Try again.")) {
            throw new InvalidSessionIDException();
        }

        if (message.equals("Text-to-speech isn't supported for this language")) {
            throw new TiktokTTSException("Seems like a typo.");
        }

        if (message.equals("Text too long to create speech audio")) {
            throw new TiktokTTSException("This is unexpected: create an issue on Github with the input data.");
        }

        byte[] b64 = Base64.getDecoder().decode(((Map<String, Object>) map.get("data")).get("v_str").toString());

        return b64;
    }

    public void createAudioFile() {
        try (FileOutputStream stream = new FileOutputStream(output)) {
            for (String speech : speeches) {
                byte[] b64 = getAudioAsBytes(speech);
                stream.write(b64);
            }
        }
    }
}
