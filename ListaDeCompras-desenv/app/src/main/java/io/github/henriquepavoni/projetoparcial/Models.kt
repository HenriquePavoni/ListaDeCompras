package io.github.henriquepavoni.projetoparcial

import java.util.UUID

data class ListaCompra(
    val id: String = UUID.randomUUID().toString(),
    var titulo: String,
    var imagemUri: String? = null
)

data class ItemCompra(
    val id: String = UUID.randomUUID().toString(),
    val listaId: String,
    var nome: String,
    var quantidade: Double,
    var unidade: String,
    var categoria: String,
    var comprado: Boolean = false
)

object Repository {
    val listas = mutableListOf<ListaCompra>()
    val itensPorLista = mutableMapOf<String, MutableList<ItemCompra>>()
}
