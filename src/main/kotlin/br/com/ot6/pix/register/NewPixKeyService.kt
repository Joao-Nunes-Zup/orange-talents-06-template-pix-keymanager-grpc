package br.com.ot6.pix.register

import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKey
import br.com.ot6.shared.clients.itau.ItauAccountsClient
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.clients.bcb.dtos.SavePixKeyRequest
import br.com.ot6.shared.exceptions.ExistingPixKeyException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class NewPixKeyService(
    @Inject val itauClient: ItauAccountsClient,
    @Inject val bcbClient: BancoCentralClient,
    @Inject val repository: PixKeyRepository
) {

    @Transactional
    fun register(@Valid pixKeyDto: NewPixKey): PixKey {
        if (repository.existsByKey(pixKeyDto.keyValue)) {
            throw ExistingPixKeyException("Chave Pix ${pixKeyDto.keyValue} já em uso")
        }

        val accountInfoReturn =
            itauClient.findByClientIdAndAccountType(
                pixKeyDto.clientId!!,
                pixKeyDto.accountType!!.name
            ).body()
                ?: throw IllegalStateException("Cliente Itaú não encontrado")

        val account = Account.from(accountInfoReturn)
        val pixKey: PixKey = pixKeyDto.toModel(account)

        repository.save(pixKey)

        val bcbPixKeyRequest = SavePixKeyRequest.from(pixKey)
        val bcbResponse = bcbClient.savePixKey(bcbPixKeyRequest)

        if (!HttpStatus.CREATED.equals(bcbResponse.status)) {
            throw IllegalStateException("Erro ao criar chave no Banco Central do Brasil")
        }

        pixKey.updateKey(bcbResponse.body()!!.key)

        return pixKey
    }
}
