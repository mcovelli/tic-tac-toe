package com.zybooks.tictactoe

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

const val HISTORY = "history_text"

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyTextView = findViewById(R.id.historyTextView)
        val historyArray = intent.getStringArrayExtra("history") ?: run {
            val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
            prefs.getString("game_history", null)?.split(";")?.toTypedArray()
        }
        historyTextView.text = historyArray?.joinToString("\n") ?: "No History Available"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(HISTORY, historyTextView.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        historyTextView.text = savedInstanceState.getString(HISTORY)
    }
}