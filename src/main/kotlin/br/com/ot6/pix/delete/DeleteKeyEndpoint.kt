package br.com.ot6.pix.delete

import br.com.ot6.*
import br.com.ot6.pix.register.NewPixKeyService
import br.com.ot6.pix.register.toDto
import br.com.ot6.shared.grpc.HandledErrors
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton

@HandledErrors
@Singleton
class DeleteKeyEndpoint(@Inject private val service: DeleteKeyService)
    : PixKeymanagerServiceGrpc.PixKeymanagerServiceImplBase() {

    override fun delete(
        request: DeletePixKeyRequest,
        responseObserver: StreamObserver<DeletePixKeyResponse>
    ) {
        service.remove(request.clientId, request.pixId)

        responseObserver.onNext(
            DeletePixKeyResponse
                .newBuilder()
                .setClientId(request.clientId)
                .setPixId(request.pixId)
                .build()
        )
        responseObserver.onCompleted()
    }
}