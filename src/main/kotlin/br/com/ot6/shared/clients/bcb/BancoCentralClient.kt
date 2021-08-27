package br.com.ot6.shared.clients.bcb

import br.com.ot6.shared.clients.bcb.dtos.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${clients.bcb.url}")
interface BancoCentralClient {

    @Post(
        value = "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        processes = [MediaType.APPLICATION_XML]
    )
    fun savePixKey(
        @Body request: SavePixKeyRequest
    ): HttpResponse<SavePixKeyReturn>

    @Delete(
        value = "/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        processes = [MediaType.APPLICATION_XML]
    )
    fun deletePixKey(
        @PathVariable key: String,
        @Body request: DeletePixKeyClientRequest
    ): HttpResponse<DeletePixKeyClientReturn>

    @Get(
        value = "/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        processes = [MediaType.APPLICATION_XML]
    )
    fun findPixKeyByKey(
        @PathVariable key: String): HttpResponse<PixKeyDetailsReturn>
}