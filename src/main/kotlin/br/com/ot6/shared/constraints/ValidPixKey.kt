package br.com.ot6.shared.constraints

import br.com.ot6.pix.register.NewPixKey
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "chave Pix inválida (\${validatedValue.keyType})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

class ValidPixKeyValidator: ConstraintValidator<ValidPixKey, NewPixKey> {

    override fun isValid(
        value: NewPixKey?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.keyType == null) {
            return false
        }

        return value.keyType.validate(value.keyValue)
    }


}
