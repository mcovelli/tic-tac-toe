package com.zybooks.tictactoe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchStartingPlayer: SwitchCompat
    private lateinit var editTextPlayers: EditText
    private var numPlayers: Int = 2 // Default to 2 players

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchStartingPlayer = findViewById(R.id.switchStartingPlayer)
        editTextPlayers = findViewById(R.id.player_edit_text)

        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)

        // ✅ Load saved settings
        switchStartingPlayer.isChecked = prefs.getBoolean("startWithO", false)
        numPlayers = prefs.getInt("numPlayers", 2) // Get saved player count
        editTextPlayers.setText(numPlayers.toString()) // Display in EditText

        // ✅ Save switch state instantly
        switchStartingPlayer.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("startWithO", isChecked).apply()
        }

        // ✅ Save player count instantly when user changes input
        editTextPlayers.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                numPlayers = s?.toString()?.toIntOrNull() ?: 2
                prefs.edit().putInt("numPlayers", numPlayers).apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ Restore state after rotation
        if (savedInstanceState != null) {
            switchStartingPlayer.isChecked = savedInstanceState.getBoolean("startWithO")
            numPlayers = savedInstanceState.getInt("numPlayers", 2)
            editTextPlayers.setText(numPlayers.toString()) // Restore EditText
        }
    }

    fun resetHistory(view: View) {
        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        // ✅ Clear stored history & reset game number
        editor.remove("game_history")
        editor.putInt("game_number", 1)
        editor.apply()

        // ✅ Show confirmation message
        Toast.makeText(this, "Game history reset successfully!", Toast.LENGTH_SHORT).show()
    }

    // ✅ Save state before screen rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("startWithO", switchStartingPlayer.isChecked)
        outState.putInt("numPlayers", numPlayers) // Save numPlayers correctly
    }
}