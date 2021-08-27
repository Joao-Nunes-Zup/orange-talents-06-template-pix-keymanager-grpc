package br.com.ot6.shared.clients.bcb.dtos

import br.com.ot6.pix.Account

data class DeletePixKeyClientRequest(
    val key: String,
) {
    val participant: String = Account.ITAU_UNIBANCO_ISPB
}
