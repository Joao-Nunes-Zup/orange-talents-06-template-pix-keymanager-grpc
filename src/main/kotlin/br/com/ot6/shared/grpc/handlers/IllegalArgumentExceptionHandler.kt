package br.com.ot6.shared.grpc.handlers

import br.com.ot6.shared.exceptions.PixKeyNotFoundException
import br.com.ot6.shared.grpc.ExceptionHandler
import br.com.ot6.shared.grpc.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import jakarta.inject.Singleton

@Singleton
class IllegalArgumentExceptionHandler: ExceptionHandler<IllegalArgumentException> {

    override fun handle(e: IllegalArgumentException): StatusWithDetails {
        return StatusWithDetails(
            Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is IllegalArgumentException
    }
}