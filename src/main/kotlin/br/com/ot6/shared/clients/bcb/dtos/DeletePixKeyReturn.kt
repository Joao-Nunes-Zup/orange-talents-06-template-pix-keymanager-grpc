package br.com.ot6.shared.clients.bcb.dtos

data class DeletePixKeyReturn(
    val key: String,
    val participant: String,
    val deletedAt: String
)
