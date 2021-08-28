package br.com.ot6.pix.list

import br.com.ot6.*
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListKeyEndpointTest(
    private val repository: PixKeyRepository,
    private val grpcClient: PixKeymanagerListServiceGrpc.PixKeymanagerListServiceBlockingStub
) {

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

    val pixKeyEmail = PixKey(
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
    fun `should list all client's keys`() {
        val response = grpcClient.list(
            ListPixKeyRequest
                .newBuilder().setClientId(CLIENT_ID.toString()).build()
        )

        response.keysList.run {
            assertThat(this, hasSize(2))
            assertThat(
                this.map { Pair(it.type, it.key) }.toList(),
                containsInAnyOrder(
                    Pair(KeyType.CPF, "14110169046"),
                    Pair(KeyType.EMAIL, "fulano@detal.com")
                )
            )
        }
    }

    @Test
    fun `should not list keys when client doesn't have keys`() {
        val clientWithNoKeysId = UUID.randomUUID().toString()

        val response = grpcClient.list(
            ListPixKeyRequest
                .newBuilder().setClientId(clientWithNoKeysId).build()
        )

        assertThat(response.keysList, hasSize(0))
    }

    @Test
    fun `should not list keys when client id is invalid`() {
        val invalidClientId = ""

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.list(
                ListPixKeyRequest
                    .newBuilder().setClientId(invalidClientId).build()
            )
        }

        exception.run {
            assertThat(status.code, equalTo(Status.INVALID_ARGUMENT.code))
            assertThat(status.description, equalTo("Id do client inválido"))
        }
    }

    @Factory
    class Clients {

        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): PixKeymanagerListServiceGrpc.PixKeymanagerListServiceBlockingStub {
            return PixKeymanagerListServiceGrpc.newBlockingStub(channel)
        }
    }
}