package br.com.ot6.pix.register

import br.com.ot6.AccountType
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKeyType
import br.com.ot6.pix.PixKey
import br.com.ot6.shared.constraints.ValidPixKey
import br.com.ot6.shared.constraints.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ValidPixKey
@Introspected
class NewPixKey(
    @field:NotBlank @ValidUUID val clientId: String?,
    @field:NotNull val keyType: PixKeyType?,
    @field:NotBlank val keyValue: String?,
    @field:NotNull val accountType: AccountType?
) {

    fun toModel(account: Account): PixKey {
        val key = when (this.keyType) {
            PixKeyType.ALEATORIA -> generateKey()
            else -> this.keyValue!!
        }

        return PixKey(
            clientId = UUID.fromString(this.clientId!!),
            type = this.keyType!!,
            key,
            accountType = this.accountType!!,
            account
        )
    }

    private fun generateKey(): String {
        return UUID.randomUUID().toString()
    }
}
