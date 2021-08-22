package br.com.ot6.shared.grpc

import br.com.ot6.shared.grpc.handlers.DefaultExceptionHandler
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.lang.IllegalStateException

@Singleton
class ExceptionHandlerResolver(
    @Inject private val handlers: List<ExceptionHandler<Exception>>
) {

    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()

    constructor(
        handlers: List<ExceptionHandler<Exception>>,
        handler: ExceptionHandler<Exception>
    ): this(handlers) {
        this.defaultHandler = handler
    }

    fun resolve(e: Exception): ExceptionHandler<Exception> {
        val foundHandlers = handlers.filter { handler -> handler.supports(e) }

        if (foundHandlers.size > 1) {
            throw IllegalStateException(
                "The exception: '${e.javaClass.name}' " +
                        "is being handled by too many handlers: $foundHandlers"
            )
        }

        return foundHandlers.firstOrNull() ?: defaultHandler
    }

}
