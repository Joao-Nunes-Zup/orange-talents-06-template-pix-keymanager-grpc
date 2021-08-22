package br.com.ot6.shared.grpc

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationTarget.*

@Around
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(CLASS, FUNCTION)
@Type(ExceptionHandlerInterceptor::class)
annotation class HandledErrors()
