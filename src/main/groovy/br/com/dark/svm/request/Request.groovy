package br.com.dark.svm.request

import br.com.dark.svm.helper.JsonHelper
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request as OkHttp3Request
import okhttp3.RequestBody
import okhttp3.Response

class Request {

    private String url
    private String method
    private Map body
    private Map multipartBody
    private Map params
    private Map headers
    private Boolean log

    private Request(Builder builder) {
        this.url = builder.getUrl()
        this.method = builder.getMethod()
        this.body = builder.getBody()
        this.params = builder.getParams()
        this.headers = builder.getHeaders()
        this.log = builder.getLog()
        this.multipartBody = builder.getMultipartBody()
    }

    protected OkHttp3Request getRequest() {
        OkHttp3Request.Builder builder = new OkHttp3Request.Builder()

        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.url).newBuilder()

        if (this.params) {
            for (Map.Entry<String, String> param : this.params.entrySet()) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        builder.url(urlBuilder.build())

        if (this.method == "GET" || this.method == "DELETE") {
            builder.method(this.method, null)
        } else {
            if (this.multipartBody && this.body) {
                throw new RuntimeException(
                        "Use 'setBody' for application/json or 'setMultipartBody' for multipart/form-data, but never use both."
                )
            }

            RequestBody body = null

            if (this.multipartBody) {
                MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)

                for (Map.Entry it : this.multipartBody) {
                    String key = it.getKey()
                    Object value = it.getValue()

                    if (value instanceof RequestFile) {
                        multipartBodyBuilder
                                .addFormDataPart(key, value.getName(), RequestBody.create(value.getContent()))
                        continue
                    }

                    multipartBodyBuilder.addFormDataPart(key, value.toString())
                }

                body = multipartBodyBuilder.build()
            }

            if (!body) {
                body = RequestBody.create(JsonHelper.toJSONObject(this.body ?: [:]).toString(),
                        MediaType.parse("application/json; charset=utf-8"))
            }

            builder.method(this.method, body)
        }

        if (this.headers) {
            for (Map.Entry header : this.headers) {
                if (header.getValue() == null) {
                    header.setValue("")
                }
            }

            builder.headers(Headers.of(this.headers))
        }

        return builder.build()
    }

    ResponseContent execute() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()

        if (this.log) {
//            clientBuilder.addInterceptor(new ResponseLoggerInterceptor()).eventListener(new MetricEventListener())
        }

        execute(clientBuilder.build())
    }

    ResponseContent execute(OkHttpClient client) {
        okhttp3.Request rest = this.getRequest()

        try (Response response = client.newCall(rest).execute()) {
            return new ResponseContent(response.code(), response.body())
        } catch (Exception e) {
            throw e
        }
    }
    static class Builder {

        private String url
        private String method = "GET"
        private Map body
        private Map params
        private Map multipartBody
        private Map headers
        private Boolean log = false

        Request build() {
            return new Request(this)
        }

        String getUrl() {
            return url
        }

        Builder setUrl(String url) {
            this.url = url
            return this
        }

        String getMethod() {
            return method
        }

        Builder setMethod(String method) {
            this.method = method?.toUpperCase()
            return this
        }

        Map getMultipartBody() {
            return multipartBody
        }

        Builder setMultipartBody(Map multipartBody) {
            this.multipartBody = multipartBody
            return this
        }

        Map getBody() {
            return body
        }

        Builder setBody(Map body) {
            this.body = body
            return this
        }

        Map getParams() {
            return params
        }

        Builder setParams(Map params) {
            this.params = params
            return this
        }

        Map getHeaders() {
            return headers
        }

        Builder setHeaders(Map headers) {
            this.headers = headers
            return this
        }

        Boolean getLog() {
            return log
        }

        Builder setLog(Boolean log) {
            this.log = log
            return this
        }
    }
}
