package io.github.henriquepavoni.projetoparcial

import java.util.UUID

data class ListaCompra(
    var id: String = UUID.randomUUID().toString(),
    var titulo: String = "",
    var imagemUri: String? = null
)

data class ItemCompra(
    var id: String = "",
    var listaId: String = "",
    var nome: String = "",
    var quantidade: Double = 1.0,
    var unidade: String = "un",
    var categoria: String = "Outros",
    var comprado: Boolean = false
)

object Repository {
    val listas = mutableListOf<ListaCompra>()
    val itensPorLista = mutableMapOf<String, MutableList<ItemCompra>>()
}
