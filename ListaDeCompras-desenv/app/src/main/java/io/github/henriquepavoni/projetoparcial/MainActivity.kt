package io.github.henriquepavoni.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
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

            val u = Session.usuarioRegistrado
            val credenciaisOk = (u != null && email == u.email && senha == u.senha)

            if (credenciaisOk) {
                val intent = Intent(this, ListasActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Login ou senha incorreto(s)", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountButton.setOnClickListener {
            val intent = Intent(this, CriarContaActivity::class.java)
            startActivity(intent)
        }
    }
}
