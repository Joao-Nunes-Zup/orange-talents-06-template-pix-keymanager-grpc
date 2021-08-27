package br.com.ot6.pix.register

import br.com.ot6.*
import br.com.ot6.pix.*
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.clients.bcb.dtos.*
import br.com.ot6.shared.clients.itau.ItauAccountsClient
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
    lateinit var itauClient: ItauAccountsClient

    @Inject
    lateinit var bcbClient: BancoCentralClient

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
            itauClient.findByClientIdAndAccountType(
                clientId = clientId.toString(),
                tipo = clientAccountType.name
            )
        ).thenReturn(HttpResponse.ok(accountInfoReturn()))

        `when`(
            bcbClient.savePixKey(
                saveKeyRequest(
                    keyType = PixKeyType.CPF,
                    key = "35504715059"
                )
            )
        ).thenReturn(HttpResponse.created(saveKeyReturn()))

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
            itauClient.findByClientIdAndAccountType(
                clientId = clientId.toString(),
                tipo = clientAccountType.name
            )
        ).thenReturn(HttpResponse.ok(accountInfoReturn()))

        `when`(
            bcbClient.savePixKey(
                saveKeyRequest(
                    keyType = PixKeyType.RANDOM,
                    key = ""
                )
            )
        ).thenReturn(HttpResponse.created(saveKeyReturn()))

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
            itauClient.findByClientIdAndAccountType(
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

    @Test
    fun `should not register pix key when bcb client fails to register pix key`() {
        `when`(
            itauClient.findByClientIdAndAccountType(
                clientId = clientId.toString(),
                tipo = clientAccountType.name
            )
        ).thenReturn(HttpResponse.ok(accountInfoReturn()))

        `when`(
            bcbClient.savePixKey(
                saveKeyRequest(
                    keyType = PixKeyType.CPF,
                    key = "35504715059"
                )
            )
        ).thenReturn(HttpResponse.notFound())

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
            assertEquals(Status.FAILED_PRECONDITION.code, exception.status.code)
            assertEquals("Erro ao criar chave no Banco Central do Brasil", exception.status.description)
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
    fun itauClient(): ItauAccountsClient {
        return Mockito.mock(ItauAccountsClient::class.java)
    }

    private fun accountInfoReturn(): AccountInfoReturn {
        return AccountInfoReturn(
            tipo = clientAccountType.name,
            instituicao = InstituicaoReturn(
                nome = "ITAÚ UNIBANCO S.A.",
                ispb = Account.ITAU_UNIBANCO_ISPB
            ),
            agencia = clientAccount.agency,
            numero = clientAccount.number,
            titular = TitularReturn(
                id = clientId.toString(),
                nome = clientAccount.ownerName,
                cpf = clientAccount.ownerCpf
            )
        )
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    private fun saveKeyRequest(keyType: PixKeyType, key: String): SavePixKeyRequest {
        return SavePixKeyRequest(
            keyType,
            key,
            bankAccount = BankAccountRequest(
                participant = Account.ITAU_UNIBANCO_ISPB,
                branch = clientAccount.agency,
                accountNumber = clientAccount.number,
                accountType = AccountTypeClientRequest.CACC
            ),
            owner = OwnerRequest(
                type = OwnerRequest.OwnerType.NATURAL_PERSON,
                name = clientAccount.ownerName,
                taxIdNumber = clientAccount.ownerCpf
            )
        )
    }

    private fun saveKeyReturn(): SavePixKeyReturn {
        return SavePixKeyReturn(
            keyType = PixKeyType.RANDOM.toString(),
            key = UUID.randomUUID().toString(),
            bankAccount = BankAccountReturn(
                participant = Account.ITAU_UNIBANCO_ISPB,
                branch = "1111",
                accountNumber = "000000",
                accountType = "CACC"
            ),
            owner = OwnerReturn(
                type = OwnerRequest.OwnerType.NATURAL_PERSON.toString(),
                name = clientAccount.ownerName,
                taxIdNumber = clientAccount.ownerCpf
            )
        )
    }
}