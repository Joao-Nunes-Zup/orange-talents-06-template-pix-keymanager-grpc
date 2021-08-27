package br.com.ot6.pix.delete

import br.com.ot6.AccountType
import br.com.ot6.DeletePixKeyRequest
import br.com.ot6.PixKeymanagerDeleteServiceGrpc
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKey
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.pix.PixKeyType
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.clients.bcb.dtos.DeletePixKeyClientRequest
import br.com.ot6.shared.clients.bcb.dtos.DeletePixKeyClientReturn
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class DeleteKeyEndpointTest(
    private val repository: PixKeyRepository,
    private val grpcClient: PixKeymanagerDeleteServiceGrpc.PixKeymanagerDeleteServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

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
        `when`(
            bcbClient.deletePixKey(
                key = existingKey.key,
                DeletePixKeyClientRequest(existingKey.key)
            )
        ).thenReturn(
            HttpResponse.ok(
                DeletePixKeyClientReturn(
                    key = existingKey.key,
                    participant = Account.ITAU_UNIBANCO_ISPB,
                    deletedAt = LocalDateTime.now().toString()
                )
            )
        )

        val response = grpcClient.delete(
            DeletePixKeyRequest
                .newBuilder()
                .setClientId(existingKey.clientId.toString())
                .setPixId(existingKey.id.toString())
                .build()
        )

        response.run {
            assertEquals(existingKey.clientId.toString(), clientId)
            assertEquals(existingKey.id.toString(), pixId)
        }
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

        exception.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(
                "Chave não encontrada ou não pertence ao cliente",
                status.description
            )
        }
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

        exception.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(
                "Chave não encontrada ou não pertence ao cliente",
                status.description
            )
        }
    }

    @Test
    fun `should not delete pix key when bcb client fail to delete it`() {
        `when`(
            bcbClient.deletePixKey(
                key = existingKey.key,
                DeletePixKeyClientRequest(existingKey.key)
            )
        ).thenReturn(HttpResponse.unprocessableEntity())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.delete(
                DeletePixKeyRequest
                    .newBuilder()
                    .setClientId(existingKey.clientId.toString())
                    .setPixId(existingKey.id.toString())
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals(
                "Erro ao remover chave no Banco Central do Brasil",
                status.description
            )
        }
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

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient {
        return Mockito.mock(BancoCentralClient::class.java)
    }
}