package io.github.henriquepavoni.projetoparcial

data class Usuario(
    val nome: String,
    val email: String,
    val senha: String
)

object Session {
    var usuarioRegistrado: Usuario? = null
}