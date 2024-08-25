package br.com.dark.svm

class UrlMappings {

    static mappings = {
        delete "/$controller/$id"(action: "delete")
        get "/$controller"(action: "index")
        get "/$controller/$id"(action: "show")
        post "/$controller"(action: "save")
        put "/$controller/$id"(action: "update")

        "/$controller/$action" {
            constraints {
                // apply constraints here
            }
        }
    }
}
