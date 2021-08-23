package br.com.ot6.pix

import br.com.ot6.AccountType
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "uk_pix_key", columnNames = ["key"])
    ]
)
class PixKey(

    @field:NotNull
    @Column(nullable = false)
    val clientId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PixKeyType,

    @field:NotBlank
    @Column(nullable = false, unique = true)
    val key: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val accountType: AccountType,

    @field:Valid
    @Embedded
    val account: Account
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val creationDate: LocalDateTime = LocalDateTime.now()
}
