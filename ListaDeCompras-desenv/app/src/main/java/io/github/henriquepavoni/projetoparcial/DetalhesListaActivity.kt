package io.github.henriquepavoni.projetoparcial

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesListaActivity : AppCompatActivity() {

    private lateinit var listaId: String
    private lateinit var tituloLista: String

    private lateinit var tvTituloLista: TextView
    private lateinit var etBuscarItens: EditText
    private lateinit var rvItens: RecyclerView
    private lateinit var fabAddItem: FloatingActionButton

    private val itensFiltrados = mutableListOf<ItemCompra>()
    private lateinit var adapter: ItensAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inside_list)

        listaId = intent.getStringExtra("LISTA_ID") ?: run { finish(); return }
        tituloLista = intent.getStringExtra("TITULO_LISTA") ?: ""

        tvTituloLista = findViewById(R.id.tvTituloLista)
        etBuscarItens = findViewById(R.id.etBuscarItens)
        rvItens = findViewById(R.id.rvItens)
        fabAddItem = findViewById(R.id.fabAddItem)

        tvTituloLista.text = "Itens da lista: $tituloLista"

        rvItens.layoutManager = LinearLayoutManager(this)
        adapter = ItensAdapter(itensFiltrados)
        rvItens.adapter = adapter

        carregarItensFirestore()

        etBuscarItens.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarItens(s?.toString().orEmpty())
            }
        })

        fabAddItem.setOnClickListener {
            mostrarDialogoItem()
        }

        val spFiltroCategoria = findViewById<Spinner>(R.id.spFiltroCategoria)
        val adpCat = ArrayAdapter.createFromResource(
            this, R.array.categorias_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spFiltroCategoria.adapter = adpCat

        spFiltroCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val categoriaSelecionada = parent.getItemAtPosition(position).toString()
                filtrarItensPorCategoria(categoriaSelecionada)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun carregarItensFirestore() {
        db.collection("listas").document(listaId).collection("itens")
            .get()
            .addOnSuccessListener { result ->
                itensFiltrados.clear()
                for (doc in result) {
                    val item = doc.toObject(ItemCompra::class.java)
                    item.id = doc.id
                    itensFiltrados.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun ordenar(items: List<ItemCompra>): List<ItemCompra> {
        return items.sortedWith(
            compareBy<ItemCompra> { it.comprado }
                .thenBy { it.nome.lowercase() }
        )
    }

    private fun filtrarItens(texto: String) {
        val base = ordenar(itensFiltrados)
        val query = texto.lowercase()
        val filtrados = if (query.isEmpty()) base else base.filter { it.nome.lowercase().contains(query) }
        itensFiltrados.clear()
        itensFiltrados.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }

    private fun filtrarItensPorCategoria(categoria: String) {
        val base = ordenar(itensFiltrados)
        val filtrados = if (categoria.equals("Todos", ignoreCase = true)) {
            base
        } else {
            base.filter { it.categoria.equals(categoria, ignoreCase = true) }
        }
        itensFiltrados.clear()
        itensFiltrados.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }

    private fun mostrarDialogoItem(itemEditando: ItemCompra? = null) {
        val view = layoutInflater.inflate(R.layout.activity_dialog_item, null)
        val etNome = view.findViewById<EditText>(R.id.etNomeItem)
        val btnMenos = view.findViewById<Button>(R.id.btnMenos)
        val btnMais = view.findViewById<Button>(R.id.btnMais)
        val tvQtd = view.findViewById<TextView>(R.id.tvQuantidadeValor)
        val spUn = view.findViewById<Spinner>(R.id.spUnidade)
        val spCat = view.findViewById<Spinner>(R.id.spCategoria)

        val adpUn = ArrayAdapter.createFromResource(
            this, R.array.unidades_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spUn.adapter = adpUn

        val adpCat = ArrayAdapter.createFromResource(
            this, R.array.categorias_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spCat.adapter = adpCat

        var qtdAtual = if (itemEditando != null) {
            val q = itemEditando.quantidade
            if (q < 1) 1 else kotlin.math.round(q).toInt()
        } else 1
        tvQtd.text = qtdAtual.toString()

        if (itemEditando != null) {
            etNome.setText(itemEditando.nome)
            spUn.setSelection((0 until adpUn.count).firstOrNull { adpUn.getItem(it)?.toString().equals(itemEditando.unidade, ignoreCase = true) } ?: 0)
            spCat.setSelection((0 until adpCat.count).firstOrNull { adpCat.getItem(it)?.toString().equals(itemEditando.categoria, ignoreCase = true) } ?: 0)
        } else {
            spUn.setSelection(adpUn.getPosition("un").takeIf { it >= 0 } ?: 0)
            spCat.setSelection(adpCat.getPosition("Outros").takeIf { it >= 0 } ?: 0)
        }

        btnMenos.setOnClickListener { if (qtdAtual > 1) tvQtd.text = (--qtdAtual).toString() }
        btnMais.setOnClickListener { tvQtd.text = (++qtdAtual).toString() }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (itemEditando == null) "Adicionar item" else "Editar item")
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nome = etNome.text.toString().trim()
            val un = spUn.selectedItem?.toString()?.trim().orEmpty().ifEmpty { "un" }
            val cat = spCat.selectedItem?.toString()?.trim().orEmpty().ifEmpty { "Outros" }

            if (nome.isEmpty()) {
                etNome.error = "Informe o nome"
                return@setOnClickListener
            }

            if (itemEditando == null) {
                val novoItem = ItemCompra(listaId = listaId, nome = nome, quantidade = qtdAtual.toDouble(), unidade = un, categoria = cat)
                db.collection("listas").document(listaId).collection("itens")
                    .add(novoItem)
                    .addOnSuccessListener { docRef ->
                        novoItem.id = docRef.id
                        itensFiltrados.add(novoItem)
                        filtrarItens(etBuscarItens.text.toString())
                        dialog.dismiss()
                    }
            } else {
                itemEditando.nome = nome
                itemEditando.quantidade = qtdAtual.toDouble()
                itemEditando.unidade = un
                itemEditando.categoria = cat
                db.collection("listas").document(listaId).collection("itens").document(itemEditando.id)
                    .set(itemEditando)
                    .addOnSuccessListener {
                        filtrarItens(etBuscarItens.text.toString())
                        dialog.dismiss()
                    }
            }
        }
    }

    private fun iconFor(categoria: String): Int {
        return when (categoria.lowercase()) {
            "frutas" -> R.drawable.frutas
            "verduras" -> R.drawable.verduras
            "carnes" -> R.drawable.carnes
            "bebidas" -> R.drawable.bebidas
            "padaria" -> R.drawable.padaria
            "higiene" -> R.drawable.higiene
            "limpeza" -> R.drawable.limpeza
            "outros" -> R.drawable.outros
            else -> R.drawable.ic_list_placeholder
        }
    }

    inner class ItensAdapter(private val itens: MutableList<ItemCompra>) :
        RecyclerView.Adapter<ItensAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val iv: ImageView = v.findViewById(R.id.ivCategoria)
            val tvNome: TextView = v.findViewById(R.id.tvNomeItem)
            val tvDetalhe: TextView = v.findViewById(R.id.tvDetalheItem)
            val cb: CheckBox = v.findViewById(R.id.cbComprado)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
            return VH(v)
        }

        override fun getItemCount(): Int = itens.size

        override fun onBindViewHolder(h: VH, position: Int) {
            val item = itens[position]

            h.iv.setImageResource(iconFor(item.categoria))
            h.tvNome.text = item.nome
            h.tvDetalhe.text = "${item.quantidade} ${item.unidade} • ${item.categoria}"

            h.itemView.setBackgroundColor(
                ContextCompat.getColor(h.itemView.context,
                    if (item.comprado) R.color.green_light else R.color.white
                )
            )

            h.cb.setOnCheckedChangeListener(null)
            h.cb.isChecked = item.comprado
            h.cb.setOnCheckedChangeListener { _, checked ->
                item.comprado = checked
                db.collection("listas").document(listaId).collection("itens").document(item.id)
                    .set(item)
                    .addOnSuccessListener { filtrarItens(etBuscarItens.text.toString()) }
            }

            h.itemView.setOnLongClickListener {
                AlertDialog.Builder(this@DetalhesListaActivity)
                    .setTitle(item.nome)
                    .setItems(arrayOf("Editar", "Excluir")) { _, which ->
                        when (which) {
                            0 -> mostrarDialogoItem(item)
                            1 -> {
                                db.collection("listas").document(listaId).collection("itens").document(item.id)
                                    .delete()
                                    .addOnSuccessListener { itensFiltrados.remove(item); filtrarItens(etBuscarItens.text.toString()) }
                            }
                        }
                    }.show()
                true
            }
        }
    }
}
