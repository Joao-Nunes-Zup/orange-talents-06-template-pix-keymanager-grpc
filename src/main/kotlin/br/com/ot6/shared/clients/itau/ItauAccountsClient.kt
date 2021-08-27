package br.com.ot6.shared.clients.itau

import br.com.ot6.pix.AccountInfoReturn
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${clients.itau.url}")
interface ItauAccountsClient {

    @Get(
        value = "/api/v1/clientes/{clientId}/contas{?tipo}",
        produces = [MediaType.APPLICATION_JSON],
        processes = [MediaType.APPLICATION_JSON]
    )
    fun findByClientIdAndAccountType(
        @PathVariable clientId: String,
        @QueryValue tipo: String
    ): HttpResponse<AccountInfoReturn>
}