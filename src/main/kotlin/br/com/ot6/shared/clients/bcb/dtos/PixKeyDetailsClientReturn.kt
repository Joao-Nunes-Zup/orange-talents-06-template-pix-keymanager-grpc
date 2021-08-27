package br.com.ot6.shared.clients.bcb.dtos

import br.com.ot6.AccountType
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKeyType
import br.com.ot6.pix.detail.PixKeyInfo
import java.time.LocalDateTime

data class PixKeyDetailsReturn(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountDetailsReturn,
    val owner: OwnerDetailsReturn,
    val createdAt: LocalDateTime
) {
    fun toPixKeyInfo(): PixKeyInfo {
        return PixKeyInfo(
            pixId = null,
            clientId = null,
            keyType = PixKeyType.valueOf(keyType),
            key,
            accountType = when (bankAccount.accountType) {
                ClientAccountType.CACC -> AccountType.CONTA_CORRENTE
                ClientAccountType.SVGS -> AccountType.CONTA_POUPANCA
            },
            account = Account(
                institution = Institutions.getName(bankAccount.participant),
                ownerName = owner.name,
                ownerCpf = owner.taxIdNumber,
                agency = bankAccount.branch,
                number = bankAccount.accountNumber
            ),
            creationDate = createdAt
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixKeyDetailsReturn

        if (keyType != other.keyType) return false
        if (key != other.key) return false
        if (bankAccount != other.bankAccount) return false
        if (owner != other.owner) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + bankAccount.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

data class BankAccountDetailsReturn(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: ClientAccountType
)

data class OwnerDetailsReturn(
    val type: String,
    val name: String,
    val taxIdNumber: String
)
