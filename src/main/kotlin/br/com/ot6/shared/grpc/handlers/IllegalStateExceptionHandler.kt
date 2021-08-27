package br.com.ot6.shared.grpc.handlers

import br.com.ot6.shared.exceptions.PixKeyNotFoundException
import br.com.ot6.shared.grpc.ExceptionHandler
import br.com.ot6.shared.grpc.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import jakarta.inject.Singleton

@Singleton
class IllegalStateExceptionHandler: ExceptionHandler<IllegalStateException> {

    override fun handle(e: IllegalStateException): StatusWithDetails {
        return StatusWithDetails(
            Status.FAILED_PRECONDITION
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is IllegalStateException
    }
}