package br.com.dark.svm.enums

enum HistoriaStatusEnum {

    OBTIDA("O"),
    CRIADA("C"),
    POSTADA("P")

    private String value

    HistoriaStatusEnum(String value) {
        this.value = value
    }

    String getValue() {
        return value
    }

    static HistoriaStatusEnum value(String value) {
        switch (value) {
            case "O":
                return OBTIDA
            case "C":
                return CRIADA
            case "P":
                return POSTADA
            default:
                return null
        }
    }

}