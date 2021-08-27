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
                    AccountTypeClientRequest.CACC
                } else {
                    AccountTypeClientRequest.SVGS
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SavePixKeyRequest

        if (keyType != other.keyType) return false
        if (key != other.key) return false
        if (bankAccount != other.bankAccount) return false
        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + bankAccount.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }

    override fun toString(): String {
        return "SavePixKeyRequest(keyType=$keyType, key='$key', bankAccount=$bankAccount, owner=$owner)"
    }


}

data class BankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeClientRequest
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
