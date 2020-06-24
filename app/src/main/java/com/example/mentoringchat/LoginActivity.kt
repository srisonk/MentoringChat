package com.example.mentoringchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
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

    private fun loginUser()
    {
        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()

         if(email == "")
        {
            Toast.makeText(this@LoginActivity, "Please specify the email.", Toast.LENGTH_LONG).show()
        }
        else if(password == "")
        {
            Toast.makeText(this@LoginActivity, "Please specify the password.", Toast.LENGTH_LONG).show()
        }
        else{
             Executors.newSingleThreadExecutor().execute {
                 //json = URL("http://10.0.2.2:5000/api/users/").readText()

                 val authObject = JSONObject()
                 authObject.put("Username",email)
                 authObject.put("Password",password)

                 post("https://mentoringacademyipb.azurewebsites.net/api/users/auth", authObject.toString())

//                 authObject.put("Username",email)
//                 authObject.put("Password",email)

                 if(userID.toInt() != 0)
                 {
                     val details: String = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$userID").readText()
                     val allUsers: String = URL("https://mentoringacademyipb.azurewebsites.net/api/users/").readText()
                     val answer = JSONArray(details)
                     val name:JSONObject = answer.get(0) as JSONObject
                     val userName = name.get("username").toString()

                     val intent = Intent(this@LoginActivity, MainActivity::class.java)
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                     intent.putExtra("UserID", userID)
                     intent.putExtra("UserName", userName)
                     intent.putExtra("AllUsers", allUsers)
                     startActivity(intent)
                     finish()
                 }
                 else
                 {
                     Toast.makeText(this@LoginActivity, "Error: Username or password incorrect!", Toast.LENGTH_LONG).show()
                 }
             }
             // Second thread goes to the DB.. 8 Secs delay to not let the code jump.
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
