package com.example.hungryme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity14 : AppCompatActivity() {

    private lateinit var labelSent: TextView
    private lateinit var resetPassword: FrameLayout
    private lateinit var backToLoginPage: ImageView
    private lateinit var editTexts: Array<EditText>
    private var verificationCode: Int = 0 // This will store the generated code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main14)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Generate a random 5-digit code
        verificationCode = (10000..99999).random()
        Toast.makeText(this, "Your verification code is: $verificationCode", Toast.LENGTH_LONG).show()

        println("Generated Code: $verificationCode") // Log it for debugging

        labelSent = findViewById(R.id.labelSent)
        resetPassword = findViewById(R.id.resetPassword)
        backToLoginPage = findViewById(R.id.backToLoginPage)

        // Find all edit texts
        editTexts = arrayOf(
            findViewById(R.id.et1),
            findViewById(R.id.et2),
            findViewById(R.id.et3),
            findViewById(R.id.et4),
            findViewById(R.id.et5)
        )

        // Auto move cursor when typing
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (count == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus()
                    } else if (count == 0 && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // This is the email coming from MainActivity4:
        val yourEmail = intent.getStringExtra("email") ?: "your email"

        // Show the fake verification message
        labelSent.text = "We sent a reset link to $yourEmail\n" +
                "Enter the 5-digit code mentioned in the email"


        // Back to login alert
        backToLoginPage.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Leave This Page?")
                .setMessage("Are you sure you want to leave? Any progress will be lost.")
                .setPositiveButton("Yes") { _, _ ->
                    startActivity(Intent(this, MainActivity2::class.java))
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Verify the code before resetting the password
        resetPassword.setOnClickListener {
            val enteredCode = editTexts.joinToString("") { it.text.toString() }.toIntOrNull()

            if (enteredCode == verificationCode) {
                val intent = Intent(this, MainActivity15::class.java)
                intent.putExtra("email", yourEmail)
                startActivity(intent)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Wrong Code!")
                    .setMessage("The code you entered is incorrect. Try again.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}
