package br.com.ot6.shared.clients.bcb.dtos

data class DeletePixKeyClientReturn(
    val key: String,
    val participant: String,
    val deletedAt: String
)
