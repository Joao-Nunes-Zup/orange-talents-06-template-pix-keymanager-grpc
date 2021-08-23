package br.com.ot6.pix

import javax.persistence.Embeddable

@Embeddable
class Account(
    val institution: String,
    val ownerName: String,
    val ownerCpf: String,
    val agency: String,
    val number: String,
)
