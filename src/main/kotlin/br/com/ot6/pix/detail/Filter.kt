package br.com.ot6.pix.detail

import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.constraints.ValidUUID
import br.com.ot6.shared.exceptions.PixKeyNotFoundException
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Suppress("CanSealedSubClassBeObject")
@Introspected
sealed class Filter {

    abstract fun filter(
        repository: PixKeyRepository,
        client: BancoCentralClient
    ): PixKeyInfo

    @Introspected
    data class ByPixId(
        @field:NotBlank @field:ValidUUID val clientId: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ): Filter() {

        fun clientIdAsUuid() = UUID.fromString(clientId)
        fun pixIdAsUuid() = UUID.fromString(pixId)

        override fun filter(
            repository: PixKeyRepository,
            client: BancoCentralClient
        ): PixKeyInfo {
            return repository
                .findById(pixIdAsUuid())
                .filter { it.belongsToClient(clientIdAsUuid()) }
                .map(PixKeyInfo::from)
                .orElseThrow { PixKeyNotFoundException("Chave Pix não encontrada") }
        }
    }

    @Introspected
    data class ByPixKey(
        @field:NotBlank @field:Size(max = 77) val key: String
    ): Filter() {

        override fun filter(
            repository: PixKeyRepository,
            client: BancoCentralClient
        ): PixKeyInfo {
            return repository
                .findByKey(key)
                .map(PixKeyInfo::from)
                .orElseGet {
                    val response = client.findPixKeyByKey(key)

                    when(response.status) {
                        HttpStatus.OK -> response.body()?.toPixKeyInfo()
                        else -> throw PixKeyNotFoundException("Chave Pix não encontrada")
                    }
                }
        }
    }

    @Introspected
    class Invalid(): Filter() {

        override fun filter(repository: PixKeyRepository, client: BancoCentralClient): PixKeyInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}
