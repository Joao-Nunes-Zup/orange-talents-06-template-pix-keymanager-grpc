package br.com.ot6.pix

import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class PixKeyType {
    CPF {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) return false
            if(!key.matches("^[0-9]{11}\$".toRegex())) return false

            return CPFValidator().run {
                this.initialize(null)
                this.isValid(key, null)
            }
        }
    },
    CELULAR {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) return false
            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) return false

            return EmailValidator().run {
                this.initialize(null)
                this.isValid(key, null)
            }
        }
    },
    ALEATORIA {
        override fun validate(key: String?): Boolean = key.isNullOrBlank()
    };

    abstract fun validate(key: String?): Boolean
}