package br.com.ot6.pix.list

import br.com.ot6.KeyType
import br.com.ot6.ListPixKeyRequest
import br.com.ot6.ListPixKeyResponse
import br.com.ot6.PixKeymanagerListServiceGrpc
import br.com.ot6.pix.PixKey
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.pix.PixKeyType
import br.com.ot6.shared.grpc.HandledErrors
import com.google.common.collect.ImmutableSet
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*

@HandledErrors
@Singleton
class ListKeyEndpoint(
    @Inject private val repository: PixKeyRepository
) : PixKeymanagerListServiceGrpc.PixKeymanagerListServiceImplBase() {

    override fun list(
        request: ListPixKeyRequest,
        responseObserver: StreamObserver<ListPixKeyResponse>
    ) {
        if (request.clientId.isNullOrBlank())
            throw IllegalArgumentException("Id do client inválido")

        val clientUUID = UUID.fromString(request.clientId)

        val keys = repository.findAllByClientId(clientUUID).map {
            ListPixKeyResponse.PixKey
                .newBuilder()
                .setPixId(it.id.toString())
                .setType(
                    when (it.type) {
                        PixKeyType.PHONE -> KeyType.CELULAR
                        PixKeyType.RANDOM -> KeyType.ALEATORIA
                        else -> KeyType.valueOf(it.type.name)
                    }
                )
                .setKey(it.key)
                .setAccountType(it.accountType)
                .setCreatedAt(
                    it.creationDate.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()

                        Timestamp
                            .newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    }
                )
                .build()
        }

        responseObserver.onNext(
            ListPixKeyResponse
                .newBuilder()
                .setClientId(request.clientId)
                .addAllKeys(keys)
                .build()
        )
        responseObserver.onCompleted()
    }
}