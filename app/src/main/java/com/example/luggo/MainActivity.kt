package com.example.luggo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null
    private var loginButton: Button? = null
    private var signupButton: Button? = null
    private var forgotPassword: TextView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Status bar rengini ayarla
        window.statusBarColor = Color.parseColor("#E31E24")
        
        // Klavyenin ekranı itmesini engelle
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            auth = FirebaseAuth.getInstance()

            // Kullanıcı zaten giriş yaptıysa direkt BavulListesiActivity'ye yönlendir
            val currentUser = auth.currentUser
            if (currentUser != null) {
                startActivity(Intent(this, BavulListesiActivity::class.java))
                finish()
                return
            }

            // View'ları bul
            emailInput = findViewById(R.id.emailInput)
            passwordInput = findViewById(R.id.passwordInput)
            loginButton = findViewById(R.id.loginButton)
            signupButton = findViewById(R.id.signupButton)
            forgotPassword = findViewById(R.id.forgotPassword)

            // View'ların null olup olmadığını kontrol et
            if (emailInput == null || passwordInput == null || loginButton == null || 
                signupButton == null || forgotPassword == null) {
                Log.e("MainActivity", "Bazı view'lar bulunamadı")
                Toast.makeText(this, "Uygulama başlatılırken bir hata oluştu", Toast.LENGTH_LONG).show()
                return
            }

            // Email input ayarları
            emailInput?.apply {
                imeOptions = EditorInfo.IME_ACTION_NEXT
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        passwordInput?.requestFocus()
                        true
                    } else {
                        false
                    }
                }
            }

            // Şifre input ayarları
            passwordInput?.apply {
                imeOptions = EditorInfo.IME_ACTION_DONE
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        hideKeyboard()
                        loginButton?.performClick()
                        true
                    } else {
                        false
                    }
                }
            }

            // Login butonu işlevi
            loginButton?.setOnClickListener {
                try {
                    hideKeyboard()
                    val email = emailInput?.text?.toString()?.trim() ?: ""
                    val password = passwordInput?.text?.toString()?.trim() ?: ""

                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Lütfen geçerli bir email adresi girin", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (password.length < 6) {
                        Toast.makeText(this, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    loginButton?.isEnabled = false
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            try {
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        Toast.makeText(this, "✅ Giriş başarılı!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, BavulListesiActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "❌ Kimlik doğrulama hatası: Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                                        loginButton?.isEnabled = true
                                    }
                                } else {
                                    // Firebase hata kontrolü
                                    val errorMessage = when (task.exception) {
                                        is FirebaseAuthInvalidUserException -> "❌ Kullanıcı kayıtlı değil! Lütfen önce kayıt olun."
                                        is FirebaseAuthInvalidCredentialsException -> "🔒 Bilgiler yanlış!"
                                        is FirebaseAuthUserCollisionException -> "⚠️ Bu email adresi ile kayıtlı bir kullanıcı zaten var!"
                                        else -> when (task.exception?.message) {
                                            "The email address is badly formatted." -> "⚠️ Geçersiz email formatı!"
                                            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "📶 Bağlantı sorunu! İnternetinizi kontrol edin."
                                            else -> "Giriş başarısız: ${task.exception?.message}"
                                        }
                                    }
                                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                                    Log.e("MainActivity", "Giriş başarısız: ${task.exception?.message}")
                                    loginButton?.isEnabled = true
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Login işlemi sırasında hata", e)
                                Toast.makeText(this, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
                                loginButton?.isEnabled = true
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainActivity", "Login işlemi başarısız", e)
                            Toast.makeText(this, "Giriş başarısız: ${e.message}", Toast.LENGTH_LONG).show()
                            loginButton?.isEnabled = true
                        }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Login butonu tıklama hatası", e)
                    Toast.makeText(this, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
                    loginButton?.isEnabled = true
                }
            }

            // Sign Up butonu işlevi
            signupButton?.setOnClickListener {
                hideKeyboard()
                startActivity(Intent(this, SignupActivity::class.java))
            }

            // Forgot Password işlevi
            forgotPassword?.setOnClickListener {
                hideKeyboard()
                val email = emailInput?.text?.toString()?.trim() ?: ""
                if (email.isEmpty()) {
                    Toast.makeText(this, "Lütfen email adresinizi girin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Lütfen geçerli bir email adresi girin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Şifre sıfırlama bağlantısı gönderildi!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Şifre sıfırlama bağlantısı gönderilemedi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Şifre sıfırlama bağlantısı gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Uygulama başlatma hatası", e)
            Toast.makeText(this, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        currentFocus?.let { view ->
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }
}