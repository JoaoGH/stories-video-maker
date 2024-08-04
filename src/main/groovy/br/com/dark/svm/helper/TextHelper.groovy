package br.com.dark.svm.helper

class TextHelper {

    static String removerEmoji(String text) {
        return text.replaceAll("[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]", "");
    }

}
