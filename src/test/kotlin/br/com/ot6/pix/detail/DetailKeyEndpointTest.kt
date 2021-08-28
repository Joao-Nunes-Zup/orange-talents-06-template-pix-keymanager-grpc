package br.com.ot6.pix.detail

import br.com.ot6.AccountType
import br.com.ot6.DetailPixKeyRequest
import br.com.ot6.PixKeymanagerDetailServiceGrpc
import br.com.ot6.PixKeymanagerRegisterServiceGrpc
import br.com.ot6.pix.Account
import br.com.ot6.pix.PixKey
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.pix.PixKeyType
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.clients.bcb.dtos.*
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class DetailKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: PixKeymanagerDetailServiceGrpc.PixKeymanagerDetailServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    private val CLIENT_ID = UUID.randomUUID()

    private val pixKeyCpf = PixKey(
        clientId = CLIENT_ID,
        type = PixKeyType.CPF,
        key = "14110169046",
        accountType = AccountType.CONTA_CORRENTE,
        account = Account(
            institution = "ITAU UNIBANCO S.A.",
            ownerName = "Fulano de Tal",
            ownerCpf = "14110169046",
            agency = "0000",
            number = "11111"
        )
    )

    private val pixKeyEmail = PixKey(
        clientId = CLIENT_ID,
        type = PixKeyType.EMAIL,
        key = "fulano@detal.com",
        accountType = AccountType.CONTA_CORRENTE,
        account = Account(
            institution = "ITAU UNIBANCO S.A.",
            ownerName = "Fulano de Tal",
            ownerCpf = "14110169046",
            agency = "0000",
            number = "11111"
        )
    )

    private val pixKeyPhone = PixKey(
        clientId = UUID.randomUUID(),
        type = PixKeyType.PHONE,
        key = "+5585988714077",
        accountType = AccountType.CONTA_CORRENTE,
        account = Account(
            institution = "ITAU UNIBANCO S.A.",
            ownerName = "Ciclano Beutrano",
            ownerCpf = "57672317024",
            agency = "0000",
            number = "11111"
        )
    )

    @BeforeEach
    fun setup() {
        repository.save(pixKeyCpf)
        repository.save(pixKeyEmail)
        repository.save(pixKeyPhone)
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `should detail pix key loaded by pixId and clientId`() {
        val existingKey = repository.findByKey("14110169046").get()

        val response = grpcClient.detail(
            DetailPixKeyRequest
                .newBuilder()
                .setPixId(
                    DetailPixKeyRequest.FilterByPixId
                        .newBuilder()
                        .setPixId(existingKey.id.toString())
                        .setClientId(existingKey.clientId.toString())
                        .build()
                )
                .build()
        )

        response.run {
            assertEquals(existingKey.id.toString(), pixId)
            assertEquals(existingKey.clientId.toString(), clientId)
            assertEquals(existingKey.type.name, key.type.name)
            assertEquals(existingKey.key, key.key)
        }
    }

    @Test
    fun `should not detail pix key loaded by empty pixId and clientId`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.detail(
                DetailPixKeyRequest
                    .newBuilder()
                    .setPixId(
                        DetailPixKeyRequest.FilterByPixId
                            .newBuilder()
                            .setPixId("")
                            .setClientId("")
                            .build()
                    )
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `should not detail pix key loaded by nonexistent pixId and clientId`() {
        val nonexistentPixId = UUID.randomUUID()
        val nonexistentClientId = UUID.randomUUID()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.detail(
                DetailPixKeyRequest
                    .newBuilder()
                    .setPixId(
                        DetailPixKeyRequest.FilterByPixId
                            .newBuilder()
                            .setPixId(nonexistentPixId.toString())
                            .setClientId(nonexistentClientId.toString())
                            .build()
                    )
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `should detail pix key loaded locally by key value`() {
        val existingKey = repository.findByKey("14110169046").get()

        val response = grpcClient.detail(
            DetailPixKeyRequest
                .newBuilder()
                .setKey(existingKey.key)
                .build()
        )

        response.run {
            assertEquals(existingKey.id.toString(), pixId)
            assertEquals(existingKey.clientId.toString(), clientId)
            assertEquals(existingKey.type.name, key.type.name)
            assertEquals(existingKey.key, key.key)
        }
    }

    @Test
    fun `should detail pix key loaded by key value not found locally but found in bcb database`() {
        val bcbResponse = pixKeyDetailsReturn()

        `when`(bcbClient.findPixKeyByKey(key = "existsthere@key.com"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsReturn()))

        val response = grpcClient.detail(
            DetailPixKeyRequest
                .newBuilder()
                .setKey("existsthere@key.com")
                .build()
        )

        response.run {
            assertEquals("", pixId)
            assertEquals("", clientId)
            assertEquals(bcbResponse.keyType, key.type.toString())
            assertEquals(bcbResponse.key, key.key)
        }
    }

    @Test
    fun `should not detail pix key loaded by key value when not found locally neither in bcb database`() {
        `when`(bcbClient.findPixKeyByKey(key = "nadaver@key.com"))
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.detail(
                DetailPixKeyRequest
                    .newBuilder()
                    .setKey("nadaver@key.com")
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `should not detail pix key loaded by key value when filter is invalid`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.detail(
                DetailPixKeyRequest
                    .newBuilder()
                    .setKey("")
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("key: must not be blank", status.description)
        }
    }

    @Test
    fun `should not detail pix key when no filter is submitted`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.detail(
                DetailPixKeyRequest.newBuilder().build()
            )
        }

        exception.run {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    private fun pixKeyDetailsReturn(): PixKeyDetailsReturn {
        return PixKeyDetailsReturn(
            keyType = PixKeyType.EMAIL.toString(),
            key = "existsthere@key.com",
            bankAccount = BankAccountDetailsReturn(
                participant = Account.ITAU_UNIBANCO_ISPB,
                branch = "0000",
                accountNumber = "11111",
                accountType = ClientAccountType.CACC
            ),
            owner = OwnerDetailsReturn(
                type = OwnerRequest.OwnerType.NATURAL_PERSON.toString(),
                name = "Nome Bacana",
                taxIdNumber = "14110169046"
            ),
            createdAt = LocalDateTime.of(2021, 9, 1, 12, 30)
        )
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {

        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): PixKeymanagerDetailServiceGrpc.PixKeymanagerDetailServiceBlockingStub?
        {
            return PixKeymanagerDetailServiceGrpc.newBlockingStub(channel)
        }
    }
}