package br.com.ot6.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface PixKeyRepository: CrudRepository<PixKey, UUID> {

    fun existsByKey(keyValue: String?): Boolean
    fun findByIdAndClientId(pixUuid: UUID?, clientUuid: UUID?): Optional<PixKey>
}
