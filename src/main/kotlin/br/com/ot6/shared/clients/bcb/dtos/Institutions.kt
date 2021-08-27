package br.com.ot6.shared.clients.bcb.dtos

class Institutions {

    companion object {

        fun getName(ispb: String): String {
            if (ispb == "60701190") return "ITAÚ UNIBANCO S.A."
            else return "Instituição não cadastrada"
        }
    }
}
