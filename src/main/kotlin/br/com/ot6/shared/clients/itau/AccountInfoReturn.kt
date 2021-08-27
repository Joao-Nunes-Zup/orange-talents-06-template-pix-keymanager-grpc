package br.com.ot6.pix

data class AccountInfoReturn(
    val tipo: String,
    val instituicao: InstituicaoReturn,
    val agencia: String,
    val numero: String,
    val titular: TitularReturn
)

data class InstituicaoReturn(val nome: String, val ispb: String)
data class TitularReturn(val id: String, val nome: String, val cpf: String)
