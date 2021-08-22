package br.com.ot6.shared.grpc.handlers

import br.com.ot6.shared.exceptions.ExistingPixKeyException
import br.com.ot6.shared.grpc.ExceptionHandler
import br.com.ot6.shared.grpc.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import jakarta.inject.Singleton

@Singleton
class ExistingPixKeyExceptionHandler: ExceptionHandler<ExistingPixKeyException> {

    override fun handle(e: ExistingPixKeyException): StatusWithDetails {
        return StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ExistingPixKeyException
    }
}