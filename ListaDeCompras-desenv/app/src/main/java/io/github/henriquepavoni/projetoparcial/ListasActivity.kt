package io.github.henriquepavoni.projetoparcial

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListasActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var uriImagemSelecionada: String? = null
    private var listaSendoEditada: ListaCompra? = null
    private var dialogoEmExibicao: AlertDialog? = null

    private val listas get() = Repository.listas
    private val listasFiltradas = mutableListOf<ListaCompra>()
    private lateinit var adapter: ListaAdapter

    private lateinit var etBuscar: EditText
    private lateinit var rvListas: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists)
        val tvSaudacao = findViewById<TextView>(R.id.tvSaudacao)
        val usuario = Session.usuarioRegistrado

        if (usuario != null) {
            tvSaudacao.text = "Olá, ${usuario.nome}!"
        }

        etBuscar = findViewById(R.id.etBuscar)
        rvListas = findViewById(R.id.rvListas)
        fabAdd = findViewById(R.id.fabAdd)
        btnLogout = findViewById(R.id.btnLogout)

        adapter = ListaAdapter(listasFiltradas)
        rvListas.layoutManager = LinearLayoutManager(this)
        rvListas.adapter = adapter

        listasFiltradas.addAll(listas)

        fabAdd.setOnClickListener {
            mostrarDialogo()
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarListas(s.toString())
            }
        })

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun mostrarDialogo(listaExistente: ListaCompra? = null) {
        listaSendoEditada = listaExistente
        uriImagemSelecionada = listaExistente?.imagemUri
        mostrarDialogoEmAndamento()
    }

    private fun mostrarDialogoEmAndamento() {
        dialogoEmExibicao?.dismiss()

        val dialogView = layoutInflater.inflate(R.layout.activity_dialog_lista, null)
        val etTitulo = dialogView.findViewById<EditText>(R.id.etTitulo)
        val ivImagemLista = dialogView.findViewById<ImageView>(R.id.ivImagemLista)
        val btnSelecionarImagem = dialogView.findViewById<Button>(R.id.btnSelecionarImagem)

        etTitulo.setText(listaSendoEditada?.titulo ?: "")

        uriImagemSelecionada?.let { uri ->
            ivImagemLista.setImageURI(android.net.Uri.parse(uri))
            ivImagemLista.visibility = android.view.View.VISIBLE
        } ?: run {
            ivImagemLista.visibility = android.view.View.GONE
        }

        btnSelecionarImagem.setOnClickListener {
            abrirGaleriaParaSelecao()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (listaSendoEditada != null) "Editar Lista" else "Adicionar lista")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()
        dialogoEmExibicao = dialog

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            if (titulo.isNotEmpty()) {
                if (listaSendoEditada != null) {
                    listaSendoEditada!!.titulo = titulo
                    listaSendoEditada!!.imagemUri = uriImagemSelecionada
                } else {
                    listas.add(ListaCompra(titulo = titulo, imagemUri = uriImagemSelecionada))
                }
                listas.sortBy { it.titulo.lowercase() }
                filtrarListas(etBuscar.text.toString())
                dialog.dismiss()
            } else {
                etTitulo.error = "Por favor, insira um título válido"
            }
        }
    }

    private fun abrirGaleriaParaSelecao() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {}
            uriImagemSelecionada = imageUri.toString()
            mostrarDialogoEmAndamento()
        }
    }

    private fun filtrarListas(texto: String) {
        listasFiltradas.clear()
        val textoLower = texto.lowercase()
        if (textoLower.isEmpty()) {
            listasFiltradas.addAll(listas)
        } else {
            listasFiltradas.addAll(listas.filter { it.titulo.lowercase().contains(textoLower) })
        }
        adapter.notifyDataSetChanged()
    }

    inner class ListaAdapter(private val listas: List<ListaCompra>) :
        RecyclerView.Adapter<ListaAdapter.ListaViewHolder>() {

        inner class ListaViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloItem)
            val ivItemLista: ImageView = itemView.findViewById(R.id.ivItemLista)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_lista, parent, false)
            return ListaViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
            val lista = listas[position]
            holder.tvTitulo.text = lista.titulo
            if (lista.imagemUri.isNullOrEmpty()) {
                holder.ivItemLista.setImageResource(R.drawable.ic_list_placeholder)
            } else {
                val imageUri = android.net.Uri.parse(lista.imagemUri)
                Glide.with(this@ListasActivity)
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_list_placeholder)
                    .into(holder.ivItemLista)
            }
            holder.itemView.setOnClickListener {
                val intent = Intent(this@ListasActivity, DetalhesListaActivity::class.java)
                intent.putExtra("LISTA_ID", lista.id)
                intent.putExtra("TITULO_LISTA", lista.titulo)
                startActivity(intent)
            }
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(this@ListasActivity)
                    .setTitle("Excluir lista")
                    .setMessage("Deseja excluir \"${lista.titulo}\"?")
                    .setPositiveButton("Excluir") { _, _ ->
                        Repository.itensPorLista.remove(lista.id)
                        Repository.listas.removeIf { it.id == lista.id }
                        filtrarListas(etBuscar.text.toString())
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
        }

        override fun getItemCount(): Int = listas.size
    }
}
