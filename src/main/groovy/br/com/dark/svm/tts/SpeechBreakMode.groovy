package br.com.dark.svm.tts

import br.com.dark.svm.helper.TextHelper

enum SpeechBreakMode {

    BREAK_ON_PUNCTUATION(){
        @Override
        List<String> getSpeechParts(String speech) {
            List<String> speeches = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            int nbOfWords = 0;
            boolean hadPunctuation = false;
            List<String> words = TextHelper.getWords(speech)

            for (String word : words) {
                nbOfWords++;
                if (builder.length() + word.length() >= 294 || nbOfWords >= 55 || hadPunctuation) {
                    speeches.add(builder.toString());
                    builder.setLength(0);
                    nbOfWords = 0;
                }
                builder.append(word).append("+");
                hadPunctuation = TextHelper.hasPunctuation(word);
            }

            speeches.add(builder.toString().stripTrailing());

            return speeches;
        }
    },
    BREAK_ON_MAX_SPEECH_SIZE(){
        @Override
        List<String> getSpeechParts(String speech) {
            List<String> speeches = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            int nbOfWords = 0;

            for (String word : TextHelper.getWords(speech)) {
                nbOfWords++;
                // 294 and 55 are based on a phenomenological study with the Test Class
                // I don't really know exactly if those data are exact. But for now it worked so...
                if (builder.length() + word.length() >= 294 || nbOfWords >= 55) {
                    speeches.add(builder.toString());
                    builder.setLength(0);
                    nbOfWords = 0;
                }
                builder.append(word).append("+");
            }

            speeches.add(builder.toString().stripTrailing());

            return speeches;
        }
    };

    abstract List<String> getSpeechParts(String speech);

    SpeechBreakMode() {
    }

}
