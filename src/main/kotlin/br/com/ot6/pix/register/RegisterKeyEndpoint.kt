package br.com.ot6.pix.register

import br.com.ot6.NewPixKeyRequest
import br.com.ot6.NewPixKeyResponse
import br.com.ot6.PixKeymanagerRegisterServiceGrpc
import br.com.ot6.shared.grpc.HandledErrors
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton

@HandledErrors
@Singleton
class RegisterKeyEndpoint(@Inject val service: NewPixKeyService)
    : PixKeymanagerRegisterServiceGrpc.PixKeymanagerRegisterServiceImplBase() {

    override fun register(
        request: NewPixKeyRequest,
        responseObserver: StreamObserver<NewPixKeyResponse>
    ) {
        val pixKeyDto = request.toDto()
        val pixKey = service.register(pixKeyDto)

        val response = NewPixKeyResponse
                        .newBuilder()
                        .setClientId(pixKey.clientId.toString())
                        .setPixId(pixKey.id.toString())
                        .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
