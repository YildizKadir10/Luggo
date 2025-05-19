package com.example.luggo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.util.Patterns

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        try {
            auth = FirebaseAuth.getInstance()

            emailInput = findViewById(R.id.emailInput)
            passwordInput = findViewById(R.id.passwordInput)
            confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
            signupButton = findViewById(R.id.signupButton)

            // Email input ayarları
            emailInput.apply {
                imeOptions = EditorInfo.IME_ACTION_NEXT
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        passwordInput.requestFocus()
                        true
                    } else {
                        false
                    }
                }
            }

            // Şifre input ayarları
            passwordInput.apply {
                imeOptions = EditorInfo.IME_ACTION_NEXT
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        confirmPasswordInput.requestFocus()
                        true
                    } else {
                        false
                    }
                }
            }

            // Şifre tekrar input ayarları
            confirmPasswordInput.apply {
                imeOptions = EditorInfo.IME_ACTION_DONE
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        signupButton.performClick()
                        true
                    } else {
                        false
                    }
                }
            }

            signupButton.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Lütfen geçerli bir email adresi girin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.length < 6) {
                    Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(this, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                signupButton.isEnabled = false
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        signupButton.isEnabled = true
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                Toast.makeText(this, "✅ Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, BavulListesiActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "❌ Kayıt başarısız: Kullanıcı oluşturulamadı", Toast.LENGTH_SHORT).show()
                                Log.e("SignupActivity", "Kullanıcı null")
                            }
                        } else {
                            val errorMessage = when (task.exception?.message) {
                                "The email address is already in use by another account." -> "Bu email adresi zaten kullanımda"
                                "The email address is badly formatted." -> "Geçersiz email formatı"
                                "The given password is invalid. [ Password should be at least 6 characters ]" -> "Şifre en az 6 karakter olmalıdır"
                                else -> "Kayıt başarısız: ${task.exception?.message}"
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e("SignupActivity", "Kayıt başarısız: ${task.exception?.message}")
                        }
                    }
                    .addOnFailureListener { e ->
                        signupButton.isEnabled = true
                        Toast.makeText(this, "Kayıt başarısız: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("SignupActivity", "Kayıt başarısız", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("SignupActivity", "Uygulama başlatma hatası", e)
            Toast.makeText(this, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}


