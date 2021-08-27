package br.com.ot6.pix.detail

import br.com.ot6.AccountType
import br.com.ot6.DetailPixKeyResponse
import br.com.ot6.KeyType
import br.com.ot6.pix.PixKeyType
import com.google.protobuf.Timestamp
import java.time.ZoneId

class DetailPixKeyResponseConverter {

    fun convert(keyInfo: PixKeyInfo): DetailPixKeyResponse {
        return DetailPixKeyResponse.newBuilder()
            .setClientId(keyInfo.clientId?.toString() ?: "")
            .setPixId(keyInfo.pixId?.toString() ?: "")
            .setKey(
                DetailPixKeyResponse.PixKey.newBuilder()
                    .setType(
                        when (keyInfo.keyType) {
                            PixKeyType.PHONE -> KeyType.CELULAR
                            PixKeyType.RANDOM -> KeyType.ALEATORIA
                            else -> KeyType.valueOf(keyInfo.keyType.name)
                        }
                    )
                    .setKey(keyInfo.key)
                    .setAccount(
                        DetailPixKeyResponse.PixKey.AccountInfo.newBuilder()
                            .setType(keyInfo.accountType)
                            .setInstitution(keyInfo.account.institution)
                            .setOwnerName(keyInfo.account.ownerName)
                            .setOwnerCpf(keyInfo.account.ownerCpf)
                            .setAgency(keyInfo.account.agency)
                            .setNumber(keyInfo.account.number)
                            .build()
                    )
                    .setCreationDate(
                        keyInfo.creationDate.let {
                            val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()

                            Timestamp.newBuilder()
                                .setSeconds(createdAt.epochSecond)
                                .setNanos(createdAt.nano)
                                .build()
                        }
                    )
                    .build()
            )
            .build()
    }
}
