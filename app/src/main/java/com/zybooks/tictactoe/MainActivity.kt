package com.zybooks.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onPlayClick(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    fun onHistoryClick(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    fun onSettingsClick(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun onExitClick(view: View) {
        finishAndRemoveTask()
    }

}