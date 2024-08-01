package br.com.dark.svm.request

import br.com.dark.svm.helper.JsonHelper
import okhttp3.ResponseBody
import org.grails.web.json.JSONElement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType


class ResponseContent {

    HttpStatus code
    JSONElement body
    String text
    MediaType mediaType
    byte[] byteStream

    ResponseContent(Integer code, ResponseBody body) {
        this.code = HttpStatus.valueOf(code)
        this.mediaType = MediaType.valueOf(body.contentType().toString().split(";")[0])
        switch (mediaType) {
            case MediaType.TEXT_PLAIN:
                this.text = body?.string()
                break
            case MediaType.APPLICATION_JSON:
                this.body = JsonHelper.toJSONElement(body?.string() ?: "{}")
                break
            default:
                this.byteStream = body?.byteStream()?.getBytes()
        }
    }

    Boolean isSuccessful() {
        return !code.isError()
    }

}
