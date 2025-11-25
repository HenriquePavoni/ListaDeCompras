package io.github.henriquepavoni.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val senha = passwordInput.text.toString()

            val emailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            if (!emailValido) {
                Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val documento = result.documents[0]
                    val senhaBanco = documento.getString("senha")

                    if (senhaBanco == senha) {
                        val nome = documento.getString("nome")

                        Session.usuarioRegistrado = Usuario(
                            nome = nome ?: "",
                            email = email,
                            senha = senha
                        )

                        val intent = Intent(this, ListasActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao consultar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        createAccountButton.setOnClickListener {
            val intent = Intent(this, CriarContaActivity::class.java)
            startActivity(intent)
        }
    }
}
