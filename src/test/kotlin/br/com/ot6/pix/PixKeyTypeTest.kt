package br.com.ot6.pix

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PixKeyTypeTest {

    @Nested
    inner class CPF {

        @Test
        fun `should be valid if cpf format is correct`() {
            PixKeyType.CPF.run {
                assertTrue(this.validate("35504715059"))
            }
        }

        @Test
        fun `should not be valid if cpf format is incorrect`() {
            PixKeyType.CPF.run {
                assertFalse(this.validate("35504715051"))
                assertFalse(this.validate("355047150591"))
                assertFalse(this.validate("3550471505a"))
            }
        }

        @Test
        fun `should not be valid if cpf is null or empty`() {
            PixKeyType.CPF.run {
                assertFalse(this.validate(""))
                assertFalse(this.validate(null))
            }
        }
    }

    @Nested
    inner class PHONE {

        @Test
        fun `should be valid if phone number format is correct`() {
            PixKeyType.PHONE.run {
                assertTrue(this.validate("+5585988714077"))
            }
        }

        @Test
        fun `should not be valid if phone number format is incorrect`() {
            PixKeyType.PHONE.run {
                assertFalse(this.validate("14998271234"))
                assertFalse(this.validate("+5a585988714077"))
            }
        }

        @Test
        fun `should not be valid if phone number is null or empty`() {
            PixKeyType.PHONE.run {
                assertFalse(this.validate(""))
                assertFalse(this.validate(null))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `should be valid if email format is correct`() {
            PixKeyType.EMAIL.run {
                assertTrue(this.validate("fulano@detal.com.br"))
                assertTrue(this.validate("fulano@detal.com"))
            }
        }

        @Test
        fun `should not be valid if email format is incorrect`() {
            PixKeyType.EMAIL.run {
                assertFalse(this.validate("fulanodetal.com.br"))
                assertFalse(this.validate("fulano@detal.com."))
            }
        }

        @Test
        fun `should not be valid if email is null or empty`() {
            PixKeyType.EMAIL.run {
                assertFalse(this.validate(""))
                assertFalse(this.validate(null))
            }
        }
    }

    @Nested
    inner class RANDOM {

        @Test
        fun `should be valid if random key comes null or valid`() {
            PixKeyType.RANDOM.run {
                assertTrue(this.validate(null))
                assertTrue(this.validate(""))
            }
        }

        @Test
        fun `should not be valid if random key has a value`() {
            PixKeyType.RANDOM.run {
                assertFalse(this.validate("value"))
            }
        }
    }
}