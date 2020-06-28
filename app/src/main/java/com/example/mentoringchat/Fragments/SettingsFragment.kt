package com.example.mentoringchat.Fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.mentoringchat.MainActivity
import com.example.mentoringchat.ModelClasses.FileDataPart
import com.example.mentoringchat.ModelClasses.Users
import com.example.mentoringchat.ModelClasses.VolleyFileUploadRequest

import com.example.mentoringchat.R
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.collections.HashMap

/**
 * This Fragment is responsible for,
 * Displaying the user profile picture, user name, and cover image
 * Permitting user to upload/edit the picture that he/she has.
 */

class SettingsFragment : Fragment()
{
    private val RequestCode = 438
    private var coverChecker: String? = ""

    lateinit var ACTIVITY: MainActivity
    private val user = Users()

    private var imageData: ByteArray? = null
    private val postURL: String = "https://mentoringacademyipb.azurewebsites.net/api/Image"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val allUsers = JSONArray(ACTIVITY.dbUsers)

        /**
         * The lower iterator first retrieves all the user from the database.
         * It does not make call to the database but instead pulls value from MainActivity
         * It then checks if the user that has logged in exist in the list.
         * When found, it stores ONLY that user to the user model class
         */

        for (i in 0 until allUsers.length()) {
            val jsonObj: JSONObject = allUsers.getJSONObject(i)
            if ((ACTIVITY.loggedUserId)!!.toInt() == jsonObj.get("user_id")) {
                user.setUID(jsonObj.get("user_id").toString())
                user.setUsername(jsonObj.get("username").toString())
                user.setGender(jsonObj.get("gender").toString())
                user.setNationality(jsonObj.get("nationality").toString())
                user.setPassword(jsonObj.get("password").toString())
                user.setBirthdate(jsonObj.get("birthdate").toString())
                user.setCourseId(jsonObj.get("course_id").toString())
                user.setProfile(jsonObj.get("profile").toString())
                user.setCover(jsonObj.get("cover").toString())
            }
        }

        /**
         * The lower condition retrieves the profile and cover image of the user from the URL(if exists) using Picasso*/

        if(context!=null)
        {
            view.username_settings.text = user.getUsername()

            if(!user.getProfile().equals("null") && !user.getProfile().equals("")){
                Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
            }
            if(!user.getCover().equals("null") && !user.getCover().equals(""))
            {
                Picasso.get().load(user.getCover()).into(view.cover_image_settings)
            }
        }

        view.profile_image_settings.setOnClickListener{
            pickImage()
        }

        view.cover_image_settings.setOnClickListener{
            coverChecker = "cover"
            pickImage()
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        ACTIVITY = context as MainActivity
    }

    private fun pickImage()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    /**
     * In the lower function, POST is made using Volley library.
     * VolleyFileUploadRequest.kt file is called and used here.
     * POST is made to the URL /api/Image*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var fileName = "image"+System.currentTimeMillis()+".jpg"
        val uri = data?.data
        if (uri != null) {
            createImageData(uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
        imageData?: return
        val request = object : VolleyFileUploadRequest(
            Method.POST,
            postURL,
            Response.Listener {
                println("response is: $it")
            },
            Response.ErrorListener {
                println("error is: $it")
            }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["file"] = FileDataPart(fileName, imageData!!, "jpeg")
                return params
            }
        }
        Volley.newRequestQueue(this.context).add(request)

        // Updating the required fields in the user table..

        val progressBar =  ProgressDialog(this.context)
        progressBar.setMessage("The image is being uploaded, please hold..")
        progressBar.show()

        val url = "https://mentoringacademyipb.azurewebsites.net/api/Gallery/$fileName"

        if(coverChecker == "cover")
        {
            user.setCover(url)

            updateUser(ACTIVITY.loggedUserId!!.toInt(), user.getUsername(), user.getGender(), user.getNationality(),
                user.getPassword(), user.getBirthdate(), user.getCourseId()!!.toInt(), user.getProfile(), user.getCover())
            coverChecker = ""
        }
        else
        {
            user.setProfile(url)

            updateUser(ACTIVITY.loggedUserId!!.toInt(), user.getUsername(), user.getGender(), user.getNationality(),
                user.getPassword(), user.getBirthdate(), user.getCourseId()!!.toInt(), user.getProfile(), user.getCover())
            coverChecker = ""
        }

        progressBar.dismiss()
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = activity!!.contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }

    /**
     * This method is used to update the URL section for the logged user when he/she decides to
     * either change the profile picture or the cover picture
     * PUT request is made to /api/user/<user_id>
     */

    private fun updateUser(user_id: Int?, username: String?, gender: String?, nationality: String?,
                           password: String?, birthdate: String? ,course_id: Int? ,profile: String? ,cover: String?)
    {
        val userObject = JSONObject()
        userObject.put("user_id",user_id)
        userObject.put("username",username)
        userObject.put("gender",gender)
        userObject.put("nationality",nationality)
        userObject.put("password",password)
        userObject.put("birthdate",birthdate)
        userObject.put("course_id",course_id)
        userObject.put("profile",profile)
        userObject.put("cover",cover)

        Executors.newSingleThreadExecutor().execute{
            put("https://mentoringacademyipb.azurewebsites.net/api/users/${ACTIVITY.loggedUserId}", userObject.toString())
        }
        Thread.sleep(1500)
    }

    fun put(url: String, body: String): String {
        return URL(url)
            .openConnection()
            .let {
                it as HttpURLConnection
            }.apply {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                requestMethod = "PUT"

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
                    //storeResponseIfNeeded = inputLine
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