package br.com.ot6.pix.delete

import br.com.ot6.AccountType
import br.com.ot6.DeletePixKeyRequest
import br.com.ot6.PixKeymanagerDeleteServiceGrpc
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKey
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.pix.PixKeyType
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class DeleteKeyEndpointTest(
    private val repository: PixKeyRepository,
    private val grpcClient: PixKeymanagerDeleteServiceGrpc.PixKeymanagerDeleteServiceBlockingStub
) {

    private lateinit var existingKey: PixKey

    @BeforeEach
    fun setup() {
        repository.deleteAll()

        val pixKey = PixKey(
            clientId = UUID.randomUUID(),
            type = PixKeyType.RANDOM,
            key = UUID.randomUUID().toString(),
            accountType = AccountType.CONTA_CORRENTE,
            account = Account(
                institution = "ITAÚ UNIBANCO S.A.",
                ownerName = "Cliente dos Santos",
                ownerCpf = "35504715059",
                agency = "0000",
                number = "11111")
        )

        existingKey = repository.save(pixKey)
    }

    @Test
    fun `should delete pix key`() {
        val response = grpcClient.delete(
            DeletePixKeyRequest
                .newBuilder()
                .setClientId(existingKey.clientId.toString())
                .setPixId(existingKey.id.toString())
                .build()
        )

        assertEquals(existingKey.clientId.toString(), response.clientId)
        assertEquals(existingKey.id.toString(), response.pixId)
    }

    @Test
    fun `should not delete an nonexistent pix key`() {
        val nonexistentPixId = UUID.randomUUID()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeletePixKeyRequest
                    .newBuilder()
                    .setClientId(existingKey.clientId.toString())
                    .setPixId(nonexistentPixId.toString())
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals(
            "Chave não encontrada ou não pertence ao cliente",
            exception.status.description
        )
    }

    @Test
    fun `should not remove pix key when the key belongs to another client`() {
        val anotherClientId = UUID.randomUUID()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeletePixKeyRequest
                    .newBuilder()
                    .setClientId(anotherClientId.toString())
                    .setPixId(existingKey.id.toString())
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, exception.status.code)
        assertEquals(
            "Chave não encontrada ou não pertence ao cliente",
            exception.status.description
        )
    }

    @Factory
    class Clients {

        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): PixKeymanagerDeleteServiceGrpc.PixKeymanagerDeleteServiceBlockingStub?
        {
            return PixKeymanagerDeleteServiceGrpc.newBlockingStub(channel)
        }
    }
}