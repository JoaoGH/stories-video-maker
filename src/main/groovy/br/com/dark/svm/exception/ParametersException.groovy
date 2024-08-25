package br.com.dark.svm.exception

import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.validation.Errors
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

class ParametersException extends AbstractApiException {

    private JSONArray errors = new JSONArray()

    ParametersException(Errors errors) {
        this(errors, 'default.api.exception.message')
    }

    ParametersException(Errors errors, String message) {
        super(message)

        for (ObjectError err : errors.allErrors) {
            this.errors.push(new JSONObject()
                    .put("field", err instanceof FieldError ? err.field : null)
                    .put("message", err.getCode())
                    .put("args", err.getArguments()))
        }
    }

    @Override
    JSONObject toJSON() {
        JSONObject json = super.toJSON()

        if (errors.size()) {
            json.put('errors', errors)
        }

        return json
    }
}
