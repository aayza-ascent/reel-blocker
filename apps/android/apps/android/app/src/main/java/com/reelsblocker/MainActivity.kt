package com.reelsblocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 120, 64, 64)
        }

        layout.addView(TextView(this).apply {
            text = "Reels Blocker"
            textSize = 28f
            setPadding(0, 0, 0, 32)
        })

        layout.addView(TextView(this).apply {
            text = "Step 1: Enable the accessibility service"
            textSize = 16f
            setPadding(0, 0, 0, 12)
        })

        layout.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        layout.addView(TextView(this).apply {
            text = "\nStep 2: Allow overlay permission"
            textSize = 16f
            setPadding(0, 24, 0, 12)
        })

        layout.addView(Button(this).apply {
            text = "Grant Overlay Permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
            }
        })

        setContentView(layout)
        RuleSyncWorker.schedule(this)
    }
}
