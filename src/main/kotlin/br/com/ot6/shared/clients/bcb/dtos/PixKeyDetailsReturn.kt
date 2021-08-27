package br.com.ot6.shared.clients.bcb.dtos

data class PixKeyDetailsReturn(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountDetailsReturn,
    val owner: OwnerDetailsReturn
)

data class BankAccountDetailsReturn(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
)

data class OwnerDetailsReturn(
    val type: String,
    val name: String,
    val taxIdNumber: String
)
