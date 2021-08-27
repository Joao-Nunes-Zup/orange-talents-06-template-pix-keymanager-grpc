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
