package br.com.ot6.pix.register

import br.com.ot6.*
import br.com.ot6.pix.*
import br.com.ot6.shared.clients.ItauAccountsClient
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
import java.util.*

@MicronautTest(transactional = false)
internal class RegisterKeyEndpointTest(
    private val pixKeyRepository: PixKeyRepository,
    private val grpcClient: PixKeymanagerRegisterServiceGrpc.PixKeymanagerRegisterServiceBlockingStub
) {

    @Inject
    lateinit var accountsClient: ItauAccountsClient

    private val clientId: UUID = UUID.randomUUID()
    private val clientAccountType: AccountType = AccountType.CONTA_CORRENTE
    private val clientAccount: Account = Account(
        institution = "ITAÚ UNIBANCO S.A.",
        ownerName = "Cliente dos Santos",
        ownerCpf = "35504715059",
        agency = "0000",
        number = "11111"
    )

    @BeforeEach
    fun setup() {
        pixKeyRepository.deleteAll()
    }

    @Test
    fun `should register a new pix key`() {
        `when`(
            accountsClient.findByClientIdAndAccountType(
                clientId = clientId.toString(),
                tipo = clientAccountType.name
            )
        ).thenReturn(HttpResponse.ok(accountInfoReturn()))

        val response = grpcClient.register(
            NewPixKeyRequest
                .newBuilder()
                .setId(clientId.toString())
                .setKeyType(KeyType.CPF)
                .setKeyValue("35504715059")
                .setAccountType(clientAccountType)
                .build()
        )

        response.run {
            assertEquals(clientId.toString(), this.clientId)
            assertNotNull(this.pixId)
        }
    }

    @Test
    fun `should not register an existing pix key`() {
        pixKeyRepository.save(
            PixKey(
                clientId,
                PixKeyType.CPF,
                "35504715059",
                clientAccountType,
                clientAccount
            )
        )

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                NewPixKeyRequest
                    .newBuilder()
                    .setId(clientId.toString())
                    .setKeyType(KeyType.CPF)
                    .setKeyValue("35504715059")
                    .setAccountType(clientAccountType)
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("Chave Pix 35504715059 já em uso", this.status.description)
        }
    }

    @Test
    fun `should not register a pix key when cpf format is invalid`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                NewPixKeyRequest
                    .newBuilder()
                    .setId(clientId.toString())
                    .setKeyType(KeyType.CPF)
                    .setKeyValue("invalid_cpf")
                    .setAccountType(clientAccountType)
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("register.pixKeyDto: chave Pix inválida", this.status.description)
        }
    }

    @Test
    fun `should not register a pix key when email format is invalid`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                NewPixKeyRequest
                    .newBuilder()
                    .setId(clientId.toString())
                    .setKeyType(KeyType.EMAIL)
                    .setKeyValue("invalid_email")
                    .setAccountType(clientAccountType)
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("register.pixKeyDto: chave Pix inválida", this.status.description)
        }
    }

    @Test
    fun `should not register a pix key when phone number format is invalid`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                NewPixKeyRequest
                    .newBuilder()
                    .setId(clientId.toString())
                    .setKeyType(KeyType.CELULAR)
                    .setKeyValue("invalid_phone_number")
                    .setAccountType(clientAccountType)
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("register.pixKeyDto: chave Pix inválida", this.status.description)
        }
    }

    @Test
    fun `random key format should be valid`() {
        `when`(
            accountsClient.findByClientIdAndAccountType(
                clientId = clientId.toString(),
                tipo = clientAccountType.name
            )
        ).thenReturn(HttpResponse.ok(accountInfoReturn()))

        val response = grpcClient.register(
            NewPixKeyRequest
                .newBuilder()
                .setId(clientId.toString())
                .setKeyType(KeyType.ALEATORIA)
                .setAccountType(clientAccountType)
                .build()
        )

        response.run {
            assertTrue(
                this.pixId.matches(
                    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$".toRegex()
                )
            )
        }
    }

    @Test
    fun `should not register pix key when client account info isn't found`() {
        `when`(
            accountsClient.findByClientIdAndAccountType(
                clientId = clientId.toString(),
                tipo = clientAccountType.name
            )
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                NewPixKeyRequest
                    .newBuilder()
                    .setId(clientId.toString())
                    .setKeyType(KeyType.ALEATORIA)
                    .setAccountType(clientAccountType)
                    .build()
            )
        }

        exception.run {
            assertEquals(Status.FAILED_PRECONDITION.code, this.status.code)
            assertEquals("Cliente Itaú não encontrado", this.status.description)
        }
    }

    @Factory
    class Clients {

        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): PixKeymanagerRegisterServiceGrpc.PixKeymanagerRegisterServiceBlockingStub?
        {
            return PixKeymanagerRegisterServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauAccountsClient::class)
    fun accountsClient(): ItauAccountsClient {
        return Mockito.mock(ItauAccountsClient::class.java)
    }

    private fun accountInfoReturn(): AccountInfoReturn {
        return AccountInfoReturn(
            tipo = clientAccountType.name,
            instituicao = InstituicaoReturn(
                nome = "ITAU UNIBANCO",
                ispb = "67829165"
            ),
            agencia = "0000",
            numero = "11111",
            titular = TitularReturn(
                id = clientId.toString(),
                nome = "Fulano de Tal",
                cpf = "87416253509"
            )
        )
    }
}