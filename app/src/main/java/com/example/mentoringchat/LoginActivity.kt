package com.example.mentoringchat

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {

    /**
     * This activity is responsible for logging user into the application
     * Verification of the credentials are done in this activity*/

    private var userID = "start"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar : Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        login_btn.setOnClickListener{
            loginUser()
        }

    }

    /**
     * This function check if the email and password field were blank or not
     * When user hits the login button,
     * It converts the credentials to JSON object and sends POST request to /api/user/auth/
     * The reply will be the user id upon success or 0 upon failure.
     * When 0, a toast is thrown and user has to retry
     * When user id, Main Activity is initialized and user ID, user name, string of all users and profile image URL of logged user is passed*/

    private fun loginUser()
    {
        val _email : TextInputLayout = findViewById(R.id.email_login)
        val _password : TextInputLayout = findViewById(R.id.password_login)

        val email: String = _email.editText!!.text.toString().trim()
        val password: String = _password.editText!!.text.toString().trim()

        if(email == "")
        {
            Toast.makeText(this@LoginActivity, "Please specify the email.", Toast.LENGTH_LONG).show()
        }
        else if(password == "")
        {
            Toast.makeText(this@LoginActivity, "Please specify the password.", Toast.LENGTH_LONG).show()
        }
        else{
            val progressBar = ProgressDialog(this)
            Executors.newSingleThreadExecutor().execute {
                this@LoginActivity.runOnUiThread{
                    progressBar.setMessage("Please hold on.. Verifying the credentials..")
                    progressBar.show()
                }

                val authObject = JSONObject()
                authObject.put("Username",email)
                authObject.put("Password",password)

                post("https://mentoringacademyipb.azurewebsites.net/api/users/auth", authObject.toString())

                if(userID.toInt() != 0)
                {
                    val details: String = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$userID").readText()
                    val allUsers: String = URL("https://mentoringacademyipb.azurewebsites.net/api/users/").readText()
                    val answer = JSONArray(details)
                    val name:JSONObject = answer.get(0) as JSONObject
                    val userName = name.get("username").toString()
                    val profile = name.get("profile").toString()

                    this@LoginActivity.runOnUiThread{
                        progressBar.dismiss()
                    }

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("UserID", userID)
                    intent.putExtra("UserName", userName)
                    intent.putExtra("AllUsers", allUsers)
                    intent.putExtra("profile", profile)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    this@LoginActivity.runOnUiThread {
                        progressBar.dismiss()
                        Toast.makeText(this@LoginActivity, "Error: Username or password incorrect!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    fun post(url: String, body: String): String {
        return URL(url)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "POST"

                doOutput = true
                val outputWriter = OutputStreamWriter(outputStream)
                outputWriter.write(body)
                outputWriter.flush()
            }.let {
                if (it.responseCode == 200) it.inputStream else it.errorStream
            }.let { streamToRead ->
                BufferedReader(InputStreamReader(streamToRead)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    userID = inputLine
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    response.toString()
                }
            }
    }
}
