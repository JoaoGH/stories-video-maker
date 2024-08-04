package br.com.dark.svm.enums

enum HistoriaOrigemEnum {

    AM_I_THE_ASSHOLE("AITA")

    private String value

    String getValue() {
        return value
    }

    HistoriaOrigemEnum(String value) {
        this.value = value
    }

    static HistoriaOrigemEnum value(String value) {
        switch (value) {
            case "AITA":
                return AM_I_THE_ASSHOLE
            default:
                return null
        }
    }
}