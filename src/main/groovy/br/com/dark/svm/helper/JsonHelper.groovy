package br.com.dark.svm.helper

import grails.artefact.DomainClass
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

class JsonHelper {

    /**
     * Transforma um Map em uma String JSON.
     *
     * */
    static String toJson(Map data) {
        return new JSON(data).toString()
    }

    /**
     * Transforma um List em uma String JSON.
     *
     * */
    static String toJson(List data) {
        return new JSON(data).toString()
    }

    /**
     * Transforma um Map em um JSONObject.
     *
     * */
    static JSONObject toJSONObject(Map data) {
        return new JSONObject(toJson(data))
    }

    /**
     * Transforma uma String em um JSONObject.
     *
     * */
    static JSONObject toJSONObject(String data) {
        return new JSONObject(data)
    }

    /**
     * Transforma um List em um JSONArray.
     *
     * */
    static JSONArray toJSONArray(List data) {
        return new JSONArray(toJson(data))
    }

    /**
     * Transforma uma String em um JSONArray.
     *
     * */
    static JSONArray toJSONArray(String data) {
        return new JSONArray(data)
    }

    static JSONElement toJSONElement(String data) {
        return JSON.parse(data)
    }

}
