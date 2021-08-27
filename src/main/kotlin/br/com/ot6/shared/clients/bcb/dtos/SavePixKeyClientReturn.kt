package br.com.ot6.shared.clients.bcb.dtos

data class SavePixKeyReturn(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountReturn,
    val owner: OwnerReturn
)

data class BankAccountReturn(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
)

data class OwnerReturn(
    val type: String,
    val name: String,
    val taxIdNumber: String
)
