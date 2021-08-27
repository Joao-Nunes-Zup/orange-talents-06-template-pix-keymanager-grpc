package br.com.ot6.pix.detail

import br.com.ot6.DetailPixKeyRequest
import br.com.ot6.DetailPixKeyResponse
import br.com.ot6.PixKeymanagerDetailServiceGrpc
import br.com.ot6.pix.PixKeyRepository
import br.com.ot6.shared.clients.bcb.BancoCentralClient
import br.com.ot6.shared.grpc.HandledErrors
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import jakarta.inject.Singleton

@HandledErrors
@Singleton
class DetailKeyEndpoint(
    @Inject private val repository: PixKeyRepository,
    @Inject private val validator: Validator,
    @Inject private val bcbClient: BancoCentralClient
): PixKeymanagerDetailServiceGrpc.PixKeymanagerDetailServiceImplBase() {

    override fun detail(
        request: DetailPixKeyRequest,
        responseObserver: StreamObserver<DetailPixKeyResponse>
    ) {
        val filter = request.toModel(validator)
        val keyInfo = filter.filter(repository, bcbClient)

        responseObserver.onNext(DetailPixKeyResponseConverter().convert(keyInfo))
        responseObserver.onCompleted()
    }
}
