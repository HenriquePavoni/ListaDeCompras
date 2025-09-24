package io.github.henriquepavoni.projetoparcial

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CriarContaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        Toast.makeText(this, "Tela de criação de conta", Toast.LENGTH_SHORT).show()
    }
}
