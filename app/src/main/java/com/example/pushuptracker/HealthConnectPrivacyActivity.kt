package com.example.pushuptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class HealthConnectPrivacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gizlilik Politikası")
                Text("Bu uygulama, egzersiz verilerinizi Health Connect üzerinden okuyarak size antrenman özetleri sunar. Verileriniz üçüncü taraflarla paylaşılmaz.")
            }
        }
    }
}
