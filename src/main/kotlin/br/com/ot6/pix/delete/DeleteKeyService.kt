package br.com.ot6.pix.delete

import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.shared.constraints.ValidUUID
import br.com.ot6.shared.exceptions.PixKeyNotFoundException
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeleteKeyService(@Inject private val repository: PixKeyRepository) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID(message = "id do cliente inválido") clientId: String?,
        @NotBlank @ValidUUID(message = "id da chave inválido") pixId: String?
    ) {
        val clientUuid = UUID.fromString(clientId)
        val pixUuid = UUID.fromString(pixId)

        val key = repository.findByIdAndClientId(pixUuid, clientUuid)
            .orElseThrow {
                PixKeyNotFoundException("Chave não encontrada ou não pertence ao cliente")
            }

        repository.deleteById(pixUuid)
    }
}
