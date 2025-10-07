package io.github.henriquepavoni.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import java.util.regex.Matcher
import java.util.regex.Pattern

class CriarContaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val newNomeInput = findViewById<EditText>(R.id.newNomeInput)
        val emailInput = findViewById<EditText>(R.id.newemailInput)
        val newSenhaInput = findViewById<EditText>(R.id.newSenhaInput)
        val newConfirmaSenhaInput = findViewById<EditText>(R.id.newConfirmaSenhaInput)
        val registerButton = findViewById<AppCompatButton>(R.id.registerButton)

        registerButton.setOnClickListener {
            val nomeText = newNomeInput.text.toString()
            val emailText = emailInput.text.toString()
            val passwordText = newSenhaInput.text.toString()
            val newConfirmaSenhaText = newConfirmaSenhaInput.text.toString()

            if (nomeText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || newConfirmaSenhaText.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if (passwordText != newConfirmaSenhaText) {
                Toast.makeText(this, "As senhas não são iguais", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(emailText)) {
                Toast.makeText(this, "O email não é valido", Toast.LENGTH_SHORT).show()
            } else {
                val novoUsuario = Usuario(
                    nome = nomeText,
                    email = emailText,
                    senha = passwordText
                )
                Session.usuarioRegistrado = novoUsuario
                Toast.makeText(this, "Conta criada! Faça login.", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        val pattern = Pattern.compile(emailPattern)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }
}
