package io.github.henriquepavoni.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)

        val imagemView: ImageView = findViewById(R.id.logoImage)

        Glide.with(this)
            .load("https://picsum.photos/id/1025/600/400")
            .fitCenter()
            .into(imagemView)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val senha = passwordInput.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if (email == "usuario@email.com" && senha == "senha123") {
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
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
