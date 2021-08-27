@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package br.com.ot6.pix.detail

import br.com.ot6.DetailPixKeyRequest
import io.micronaut.validation.validator.Validator
import javax.validation.ConstraintViolationException

fun DetailPixKeyRequest.toFilter(validator: Validator): Filter {

    val filter = when (filterCase) {
        DetailPixKeyRequest.FilterCase.PIXID -> pixId.let {
            Filter.ByPixId(it.clientId, it.pixId)
        }
        DetailPixKeyRequest.FilterCase.KEY -> Filter.ByPixKey(key)
        DetailPixKeyRequest.FilterCase.FILTER_NOT_SET -> Filter.Invalid()
    }

    val violations = validator.validate(filter)

    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filter
}