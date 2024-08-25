package br.com.dark.svm.enums

enum BackgroundVideoEnum {

    MINECRAFT("M", "minecraft.mp4")

    private String value
    private String videoName

    String getValue() {
        return value
    }

    String getVideoName() {
        return videoName
    }

    BackgroundVideoEnum(String value, String videoName) {
        this.value = value
        this.videoName = videoName
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
