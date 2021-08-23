package br.com.ot6.pix.register

import br.com.ot6.pix.PixKey
import br.com.ot6.shared.clients.ItauAccountsClient
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.shared.exceptions.ExistingPixKeyException
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class NewPixKeyService(
    @Inject val accountsClient: ItauAccountsClient,
    @Inject val repository: PixKeyRepository
) {

    @Transactional
    fun register(@Valid pixKeyDto: NewPixKey): PixKey {
        if (repository.existsByKey(pixKeyDto.keyValue)) {
            throw ExistingPixKeyException("Chave Pix ${pixKeyDto.keyValue} já em uso")
        }

        val accountInfoReturn =
            accountsClient.findByClientIdAndAccountType(
                pixKeyDto.clientId!!,
                pixKeyDto.accountType!!.name
            )

        val account = accountInfoReturn.body()?.toModel()
            ?: throw IllegalStateException("Cliente Itaú não encontrado")

        val pixKey: PixKey = pixKeyDto.toModel(account)
        repository.save(pixKey)

        return pixKey
    }
}
