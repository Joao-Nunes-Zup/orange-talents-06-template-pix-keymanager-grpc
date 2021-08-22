package br.com.ot6.pix

data class AccountInfoReturn(
    val tipo: String,
    val instituicao: InstituicaoReturn,
    val agencia: String,
    val numero: String,
    val titular: TitularReturn
) {

    fun toModel(): Account {
        return Account(
            institution = this.instituicao.nome,
            ownerName = this.titular.nome,
            ownerCpf = this.titular.cpf,
            agency = this.agencia,
            number = this.numero,
            accountNumber = this.numero
        )
    }
}

data class InstituicaoReturn(val nome: String, val ispb: String)
data class TitularReturn(val id: String, val nome: String, val cpf: String)
