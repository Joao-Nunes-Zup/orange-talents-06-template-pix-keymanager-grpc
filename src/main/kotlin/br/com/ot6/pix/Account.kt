package br.com.ot6.pix

import br.com.ot6.AccountType
import javax.persistence.Embeddable

@Embeddable
class Account(
    val institution: String,
    val ownerName: String,
    val ownerCpf: String,
    val agency: String,
    val number: String,
) {

    companion object {
        val ITAU_UNIBANCO_ISPB: String = "60701190"

        fun from(infoReturn: AccountInfoReturn): Account {
            return Account(
                institution = infoReturn.instituicao.nome,
                ownerName = infoReturn.titular.nome,
                ownerCpf = infoReturn.titular.cpf,
                agency = infoReturn.agencia,
                number = infoReturn.numero
            )
        }
    }
}
