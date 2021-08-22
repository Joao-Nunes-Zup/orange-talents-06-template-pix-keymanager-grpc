package br.com.ot6.pix.register

import br.com.ot6.AccountType
import br.com.ot6.KeyType
import br.com.ot6.NewPixKeyRequest
import br.com.ot6.pix.PixKeyType

fun NewPixKeyRequest.toDto(): NewPixKey {
    return NewPixKey(
        clientId = this.id,
        keyType = when (this.keyType) {
            KeyType.UNKNOWN_KEY_TYPE -> null;
            else -> PixKeyType.valueOf(this.keyType.name)
        },
        keyValue = this.keyValue,
        accountType = when (this.accountType) {
            AccountType.UNKNOWN_ACCOUNT_TYPE -> null;
            else -> this.accountType
        },
    )
}