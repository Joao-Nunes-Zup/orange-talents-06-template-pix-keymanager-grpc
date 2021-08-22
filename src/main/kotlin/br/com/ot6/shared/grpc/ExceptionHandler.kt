package br.com.ot6.shared.grpc

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

interface ExceptionHandler<E: Exception> {

    fun handle(e: E): StatusWithDetails

    fun supports(e: Exception): Boolean

    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()) {

        constructor(sre: StatusRuntimeException):
                this(sre.status, sre.trailers ?: Metadata())

        constructor(sp: com.google.rpc.Status):
                this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException(metadata)
        }
    }
}