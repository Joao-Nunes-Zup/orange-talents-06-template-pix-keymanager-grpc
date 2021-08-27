package br.com.ot6.pix.detail

import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.clients.itau.ItauAccountsClient
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class DetailKeyService(
    @Inject val repository: PixKeyRepository,
    @Inject val bcbClient: BancoCentralClient
) {

    fun findKeyInfo(@Valid filter: Filter): PixKeyInfo {
        return filter.filter(repository, bcbClient)
    }
}
