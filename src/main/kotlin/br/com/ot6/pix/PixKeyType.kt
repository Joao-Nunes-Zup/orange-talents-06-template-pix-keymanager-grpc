package br.com.ot6.pix

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator


enum class PixKeyType {
    CPF {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) return false
            if (key.count() > 11) return false

            return CPFValidator().run {
                initialize(null)
                isValid(key, null)
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
                initialize(null)
                isValid(key, null)
            }
        }
    },
    ALEATORIA {
        override fun validate(key: String?): Boolean = key.isNullOrBlank()
    };

    abstract fun validate(key: String?): Boolean
}