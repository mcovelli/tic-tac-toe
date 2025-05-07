package com.zybooks.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var buttons: Array<Button>
    private var isPlayerX = true // Track current player
    private var board = Array(3) { Array(3) { "" } } // Tic Tac Toe board
    private var winner: String? = null // Track the winner
    private var numPlayers = 2 // Default to 2 players
    private val history = mutableListOf<String>() // Stores game results
    private var gameNumber = 1 // Keeps track of game numbers

    fun openHistory(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.putExtra("history", history.toTypedArray()) // Ensure history is sent
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Initialize Tic Tac Toe buttons
        buttons = arrayOf(
            findViewById(R.id.button1), findViewById(R.id.button2), findViewById(R.id.button3),
            findViewById(R.id.button4), findViewById(R.id.button5), findViewById(R.id.button6),
            findViewById(R.id.button7), findViewById(R.id.button8), findViewById(R.id.button9)
        )

        for (button in buttons) {
            button.setOnClickListener { onButtonClick(button) }
        }

        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        isPlayerX = !prefs.getBoolean("startWithO", false)
        numPlayers = prefs.getInt("numPlayers", 2)
        gameNumber = prefs.getInt("game_number", 1)

        //Load saved history
        loadHistory()

        // If it's a 1-player game and O starts, AI should make the first move
        if (numPlayers == 1 && !isPlayerX) {
            aiMove()
        }

        // Restore game state after rotation
        if (savedInstanceState != null) {
            restoreGameState(savedInstanceState)
        }

        findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }
    }

    private fun loadHistory() {
        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        val savedHistory = prefs.getString("game_history", null)

        if (!savedHistory.isNullOrEmpty()) {
            history.clear()
            history.addAll(savedHistory.split(";")) // Convert back to list
        }
    }

    private fun onButtonClick(button: Button) {
        if (button.text.isNotEmpty() || winner != null) return // Stop game if winner exists

        val symbol = if (isPlayerX) "X" else "O"
        button.text = symbol
        updateBoard(button, symbol)

        winner = checkWinner()
        if (winner != null) {
            val winningPlayer = if (winner == "X") "Player 1" else "Player 2"
            val gameResult = String.format("%-20s %-25s %-10s", "$gameNumber", winningPlayer, winner)

            history.add(gameResult) // Add result to list
            saveHistory() // ✅ Save history to SharedPreferences
            gameNumber++ // Increment game count
            saveGameNumber()

            Toast.makeText(this, "$winner wins!", Toast.LENGTH_SHORT).show()
            disableBoard()
            return
        }

        isPlayerX = !isPlayerX // Switch player

        // If it's a 1-player game and it's AI's turn, let AI move
        if (numPlayers == 1 && !isPlayerX) {
            aiMove()
        }
    }

    private fun saveGameNumber() {
        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt("game_number", gameNumber)
        editor.apply() // ✅ Save changes
    }

    private fun saveHistory() {
        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        // Convert history list into a single string (separated by `;`)
        editor.putString("game_history", history.joinToString(";"))
        editor.apply() // Save changes
    }

    private fun aiMove() {
        // Get a list of empty buttons (cells)
        val emptyCells = buttons.filter { it.text.isEmpty() }

        // If there are no empty cells, do nothing
        if (emptyCells.isEmpty()) return

        // Try to find a winning move for AI ("O")
        var chosenButton: Button? = null
        for (button in emptyCells) {
            // Get the index, row, and column for the button
            val index = buttons.indexOf(button)
            val row = index / 3
            val col = index % 3

            // Simulate AI move by placing "O" temporarily
            board[row][col] = "O"
            if (checkWinner() == "O") {
                // Found a winning move, choose this button
                chosenButton = button
                // Revert board to empty at this cell (will be updated below)
                board[row][col] = ""
                break
            }
            // Revert temporary move
            board[row][col] = ""
        }

        // If no winning move was found, try to block opponent ("X")
        if (chosenButton == null) {
            for (button in emptyCells) {
                val index = buttons.indexOf(button)
                val row = index / 3
                val col = index % 3

                // Simulate opponent's move by placing "X" temporarily
                board[row][col] = "X"
                if (checkWinner() == "X") {
                    // This move would allow opponent to win; choose this cell to block
                    chosenButton = button
                    board[row][col] = ""
                    break
                }
                board[row][col] = ""
            }
        }

        // If neither winning nor blocking move was found, choose a random empty cell
        if (chosenButton == null) {
            chosenButton = emptyCells.random()
        }

        // Now, perform the chosen move for AI:
        chosenButton.text = "O" // Set the text of the chosen button to "O"
        updateBoard(chosenButton, "O") // Update the internal board array

        // Check if this move results in a win
        winner = checkWinner()
        if (winner != null) {
            val winningPlayer = if (winner == "X") "Player 1" else "Player 2"
            val gameResult = String.format("%-20s %-25s %-10s", "$gameNumber", winningPlayer, winner)
            history.add(gameResult) // Add result to history list
            saveHistory() // Save history to SharedPreferences
            gameNumber++ // Increment game count
            saveGameNumber() // Save updated game number
            Toast.makeText(this, "$winner wins!", Toast.LENGTH_SHORT).show()
            disableBoard() // Disable further moves
        }

        // Switch the turn back to the human player (X)
        isPlayerX = true
    }

    private fun updateBoard(button: Button, symbol: String) {
        val index = buttons.indexOf(button)
        val row = index / 3
        val col = index % 3
        board[row][col] = symbol
    }

    private fun checkWinner(): String? {
        // Check rows, columns, diagonals
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != "")
                return board[i][0] // Row win
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != "")
                return board[0][i] // Column win
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != "")
            return board[0][0] // Diagonal win
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != "")
            return board[0][2] // Anti-diagonal win

        return null
    }

    private fun disableBoard() {
        for (button in buttons) button.isEnabled = false
    }

    private fun resetGame() {
        for (button in buttons) {
            button.text = "" // Clear button text
            button.isEnabled = true // Re-enable buttons
        }
        board = Array(3) { Array(3) { "" } } // Clear board array
        winner = null // Reset winner state

        val prefs = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        isPlayerX = !prefs.getBoolean("startWithO", false) // Reload starting player

        // If AI is starting, let it move first
        if (numPlayers == 1 && !isPlayerX) {
            aiMove()
        }

        Toast.makeText(this, "Game Reset!", Toast.LENGTH_SHORT).show()
    }

    // Save game state (Button texts, current player, winner) before rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val buttonTexts = buttons.map { it.text.toString() }.toTypedArray()
        outState.putStringArray("button_texts", buttonTexts)
        outState.putBoolean("isPlayerX", isPlayerX)
        outState.putString("winner", winner)
        outState.putInt("game_number", gameNumber) // ✅ Save game number
    }

    // Restore game state after rotation
    private fun restoreGameState(savedInstanceState: Bundle) {
        val buttonTexts = savedInstanceState.getStringArray("button_texts")
        isPlayerX = savedInstanceState.getBoolean("isPlayerX", true)
        winner = savedInstanceState.getString("winner")
        gameNumber = savedInstanceState.getInt("game_number", 1) // ✅ Restore game number

        if (buttonTexts != null) {
            for (i in buttons.indices) {
                buttons[i].text = buttonTexts[i]
            }
        }

        // If there was a winner before rotation, disable the board
        if (winner != null) {
            Toast.makeText(this, "$winner wins!", Toast.LENGTH_SHORT).show()
            disableBoard()
        }

        // Ensure AI makes the first move if O starts
        if (numPlayers == 1 && !isPlayerX && winner == null) {
            aiMove()
        }
    }
}