package br.com.dark.svm.request

class RequestFile {

    String name
    byte[] content

    int getSize() {
        return this.content.size()
    }
}
