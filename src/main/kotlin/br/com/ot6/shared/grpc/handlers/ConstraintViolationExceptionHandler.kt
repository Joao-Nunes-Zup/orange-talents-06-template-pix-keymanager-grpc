package br.com.ot6.shared.grpc.handlers

import br.com.ot6.shared.grpc.ExceptionHandler
import br.com.ot6.shared.grpc.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationExceptionHandler: ExceptionHandler<ConstraintViolationException> {

    override fun handle(e: ConstraintViolationException): StatusWithDetails {
        return StatusWithDetails(
            Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }
}