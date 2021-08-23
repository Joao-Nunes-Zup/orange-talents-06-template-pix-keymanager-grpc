package br.com.ot6.shared.grpc.handlers

import br.com.ot6.shared.exceptions.PixKeyNotFoundException
import br.com.ot6.shared.grpc.ExceptionHandler
import br.com.ot6.shared.grpc.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import jakarta.inject.Singleton

@Singleton
class PixKeyNotFoundExceptionHandler: ExceptionHandler<PixKeyNotFoundException> {

    override fun handle(e: PixKeyNotFoundException): StatusWithDetails {
        return StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is PixKeyNotFoundException
    }
}