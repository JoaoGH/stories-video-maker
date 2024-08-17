package br.com.dark.svm.helper

class TextHelper {

    static String removerEmoji(String text) {
        return text.replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]", "");
    }

    static String[] getWords(String speech) {
        getWords(speech, new Locale("pt", "BR"))
    }

    static String[] getWords(String speech, Locale locale) {
        speech = speech.replaceAll("\\+", "plus")
        speech = speech.replaceAll(" ", "+")
        speech = speech.replaceAll("&", getArtigoEByLocale(locale.toString()))

        return speech.split("\\+");
    }

    static String getArtigoEByLocale(String locale) {
        switch (locale) {
            case "pt_BR":
                return "e"
            default:
                return "and"
        }
    }
}
