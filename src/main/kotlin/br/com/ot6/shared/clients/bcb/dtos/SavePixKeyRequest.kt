package br.com.ot6.shared.clients.bcb.dtos

import br.com.ot6.AccountType
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKey
import br.com.ot6.pix.PixKeyType

data class SavePixKeyRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccountRequest,
    val owner: OwnerRequest
) {
    companion object {

        fun from(pixKey: PixKey): SavePixKeyRequest {
            val accountType =
                if (AccountType.CONTA_CORRENTE.equals(pixKey.accountType)) {
                    AccountTypeRequest.CACC
                } else {
                    AccountTypeRequest.SVGS
                }

            return SavePixKeyRequest(
                keyType = pixKey.type,
                key = pixKey.key,
                bankAccount = BankAccountRequest(
                    participant = Account.ITAU_UNIBANCO_ISPB,
                    branch = pixKey.account.agency,
                    accountNumber = pixKey.account.number,
                    accountType
                ),
                owner = OwnerRequest(
                    type = OwnerRequest.OwnerType.NATURAL_PERSON,
                    name = pixKey.account.ownerName,
                    taxIdNumber = pixKey.account.ownerCpf
                ),
            )
        }
    }
}

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeRequest
)

data class OwnerRequest(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {

    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}
