package br.com.ot6.pix.detail

import br.com.ot6.AccountType
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKey
import br.com.ot6.pix.PixKeyType
import java.time.LocalDateTime
import java.util.*

class PixKeyInfo(
    val pixId: UUID?,
    val clientId: UUID?,
    val keyType: PixKeyType,
    val key: String,
    val accountType: AccountType,
    val account: Account,
    val creationDate: LocalDateTime
) {

    companion object {

        fun from(pixKey: PixKey): PixKeyInfo {
            return PixKeyInfo(
                pixId = pixKey.id,
                clientId = pixKey.clientId,
                keyType = pixKey.type,
                key = pixKey.key,
                accountType = pixKey.accountType,
                account = pixKey.account,
                creationDate = pixKey.creationDate
            )
        }
    }

    override fun toString(): String {
        return "PixKeyInfo(pixId=$pixId, clientId=$clientId, keyType=$keyType, key='$key', accountType=$accountType, account=$account, creationDate=$creationDate)"
    }


}
