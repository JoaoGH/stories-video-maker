package br.com.dark.svm.enums

enum BackgroundVideoEnum {

    MINECRAFT("M", "minecraft")

    private String value
    private String folder

    String getValue() {
        return value
    }

    String getFolder() {
        return folder
    }

    BackgroundVideoEnum(String value, String folder) {
        this.value = value
        this.folder = folder
    }

    static BackgroundVideoEnum value(String value) {
        switch (value) {
            case "M":
                return MINECRAFT
            default:
                return null
        }
    }

}
