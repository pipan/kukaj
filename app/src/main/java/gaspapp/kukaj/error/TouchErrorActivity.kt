package gaspapp.kukaj.error

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import gaspapp.kukaj.browse.TouchMainActivity
import gaspapp.kukaj.detail.DetailsActivity
import gaspapp.kukaj.theme.KukajTheme

class TouchErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val errorMessage = intent.getSerializableExtra("ErrorMessage") as String? ?: "neočakávana chyba"
        setContent {
            KukajTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Chyba",
                            fontSize = 6.em
                        )
                        Text( text = errorMessage )
                        Button(
                            onClick = {
                                val intent = Intent(applicationContext, TouchMainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        ) {
                            Text(text = "Zoznam videí")
                        }
                    }
                }
            }
        }
    }
}