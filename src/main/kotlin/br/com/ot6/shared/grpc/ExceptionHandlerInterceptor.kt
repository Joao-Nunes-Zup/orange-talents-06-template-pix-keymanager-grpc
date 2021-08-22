package br.com.ot6.shared.grpc

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@InterceptorBean(HandledErrors::class)
class ExceptionHandlerInterceptor(
    @Inject private val resolver: ExceptionHandlerResolver
): MethodInterceptor<BindableService, Any?> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        return try {
            context.proceed()
        } catch (e: Exception) {
            logger.error(
                "Exception '${e.javaClass.name}' " +
                        "intercepted when '${context.targetMethod}' call was processing", e)

            val handler = resolver.resolve(e)
            val status = handler.handle(e)

            GrpcEndpointArguments(context).response().onError(status.asRuntimeException())

            null
        }
    }

    private class GrpcEndpointArguments(val context : MethodInvocationContext<BindableService, Any?>) {

        fun response(): StreamObserver<*> {
            return context.parameterValues[1] as StreamObserver<*>
        }
    }
}