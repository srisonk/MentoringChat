package com.example.mentoringchat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.mentoringchat.AdapterClasses.ChatsAdapter
import com.example.mentoringchat.ModelClasses.Chat
import com.example.mentoringchat.ModelClasses.FileDataPart
import com.example.mentoringchat.ModelClasses.VolleyFileUploadRequest
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageChatActivity : AppCompatActivity()
{
    var userIdVisit: String = ""
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_view_chats: RecyclerView
    var loggedUser: String = ""
    var allUsers: String = ""
    var userName: String = ""
    var profile: String = ""

    private var imageData: ByteArray? = null
    private val postURL: String = "https://mentoringacademyipb.azurewebsites.net/api/Image"

    private var userNameRec:String? = ""
    private var profileRec:String? = ""

    companion object {
        private const val IMAGE_PICK_CODE = 999
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@MessageChatActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("UserID",loggedUser)
            intent.putExtra("UserName",userName)
            intent.putExtra("AllUsers",allUsers)
            intent.putExtra("profile",profile)
            startActivity(intent)
            finish()
        }

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        loggedUser = intent.getStringExtra("user_id")
        allUsers = intent.getStringExtra("AllUsers")
        userName = intent.getStringExtra("UserName")
        profile = intent.getStringExtra("profile")
        //firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManger = LinearLayoutManager(applicationContext)
        linearLayoutManger.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManger

        /**
         * The lower thread is executed so that it will leave the UI thread, enter the network thread and,
         * GET the details of the user that is supposed to receive the message,
         * when details received, it adds the username and profile picture of the receiver to the Activity.*/

        Executors.newSingleThreadExecutor().execute{
            val receiverName = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$userIdVisit").readText()
            val answer = JSONArray(receiverName)
            val name:JSONObject = answer.get(0) as JSONObject

            userNameRec = name.get("username").toString()
            profileRec = name.get("profile").toString()

            username_mchat.text = userNameRec

            this@MessageChatActivity.runOnUiThread {
                Picasso.get().load(profileRec).into(profile_image_mChat)
            }
        }

        retrieveMessages(loggedUser, userIdVisit, profileRec)


        /**
         * when send message button is tapped, sendMessageToUser() function is called passing the
         * userId of the currently logged user,
         * userId of the receiver,
         * message content and,
         * URL but blank since it is a text message.
         */
        send_message_btn.setOnClickListener {
            val message = text_message.text.toString()
            if (message == "")
            {
                Toast.makeText(this@MessageChatActivity, "Please write a message", Toast.LENGTH_LONG).show()
            }
            else
            {
                sendMessageToUser(loggedUser, userIdVisit, message,"")
                val chat = Chat()
                chat.setMessage(message)
                chat.setReceiver(userIdVisit)
                chat.setSender(loggedUser)
                (mChatList as ArrayList<Chat>).add(chat)
                filter(loggedUser,userIdVisit)
            }
            text_message.setText("")
        }

        /**
         * when image button is tapped,
         * user gets to select an image from the gallery that he wants to send as a message,
         * and rest of the execution is done in OnActivityResult()
         */

        attach_image_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select an image"), IMAGE_PICK_CODE)
        }

        seenMessage(userIdVisit)
    }

    /**
     * The lower function when called upon the button click,
     * converts all the fields to JSON object and makes ready to POST to /api/messages,
     * converts logged user ID and receiver user ID to JSON object and makes ready to POST to /api/chatList
     * Both of the objects are converted to strings and then POST request is made to respective URLs*/

    @SuppressLint("SimpleDateFormat")
    private fun sendMessageToUser(senderId: String?, receiverId: String?, message: String, url: String?)
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val messageObject = JSONObject()
        messageObject.put("Id_sender",senderId!!.toInt())
        messageObject.put("Id_receiver",receiverId!!.toInt())
        messageObject.put("content",message)
        messageObject.put("TimeStamp",currentDate)
        messageObject.put("url",url)

        val messageListObject = JSONObject()
        messageListObject.put("Id_sender",senderId!!.toInt())
        messageListObject.put("Id_receiver",receiverId!!.toInt())

        Executors.newSingleThreadExecutor().execute{
            post("https://mentoringacademyipb.azurewebsites.net/api/messages", messageObject.toString())
            post("https://mentoringacademyipb.azurewebsites.net/api/chatList", messageListObject.toString())
        }
        Thread.sleep(1500)
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


    /**
     * The lower function retrieves the messages between users and displays them
     * A GET request is made to /api/messages
     * Messages are converted to JSON array and is array is iterated and converted to JSON object.
     * In each object, it is checked if the sender id is equal to the logged user id and,
     * receiver id is equal to the user that has been selected to send message to.
     * When the match is found, a chatList is created and is passed to the ChatsAdapter in order to display the messages in correct places.
     */
    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?)
    {
        mChatList = ArrayList()
        Executors.newSingleThreadExecutor().execute{
            val allMessagesString: String = URL("https://mentoringacademyipb.azurewebsites.net/api/messages").readText()
            val allMessages = JSONArray(allMessagesString)
            for (i in 0 until allMessages.length()) {
                val jsonObj: JSONObject = allMessages.getJSONObject(i)
                val chat = Chat()
                chat.setMessage(jsonObj.get("content").toString())
                chat.setMessageId(jsonObj.get("msg_id").toString())
                chat.setReceiver(jsonObj.get("id_receiver").toString())
                chat.setSender(jsonObj.get("id_sender").toString())
                chat.setUrl(jsonObj.get("url").toString())
                if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId) ||
                    chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                {
                    (mChatList as ArrayList<Chat>).add(chat)
                    chatsAdapter!!.notifyDataSetChanged()
                }
                chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), loggedUser, receiverImageUrl!!)
                recycler_view_chats.adapter = chatsAdapter
            }
        }
        Thread.sleep(1500)
    }

    /**
     * This method helps is re-rendering the current activity by updating the message list.
     * It is called when user sends the message to the receiver.
     * When the if condition is verified, a new ArrayList of type Chat gets contents added,
     * ChatsAdapter is recalled in order to stack the new messages to the view.
     */
    private fun filter(senderId: String, receiverId: String?){
        val myList = ArrayList<Chat>()

        for(s in (mChatList as ArrayList<Chat>)){
            if(s!!.getReceiver().equals(senderId) && s.getSender().equals(receiverId) || s.getReceiver().equals(receiverId) && s.getSender().equals(senderId)){
                myList.add(s)
            }
        }
        chatsAdapter = ChatsAdapter(this@MessageChatActivity, myList, loggedUser, profileRec!!)
        recycler_view_chats.adapter = chatsAdapter
    }


    private fun seenMessage(userId: String)
    {
        val chat = Chat()

        if(chat!!.getReceiver().equals(loggedUser) && chat!!.getSender().equals(userId))
        {
            chat.setIsSeen(true)
        }
    }

    /**
     * The lower method is executed when user selects an image from the gallery.
     * The picked image is converted into byte array and POST is requested to /api/Image using Volley library
     * File name is provided using the current system time in millisecond.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        if (uri != null) {
            createImageData(uri)
        }
        super.onActivityResult(requestCode, resultCode, data)

        var fileName = "image"+System.currentTimeMillis()+".jpg"

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
        Volley.newRequestQueue(this).add(request)

        val message = "sent you an image."
        val url = "https://mentoringacademyipb.azurewebsites.net/api/Gallery/$fileName"

        sendMessageToUser(loggedUser,userIdVisit,message,url)
        val chat = Chat()
        chat.setMessage(message)
        chat.setReceiver(userIdVisit)
        chat.setSender(loggedUser)
        chat.setUrl(url)
        (mChatList as ArrayList<Chat>).add(chat)
        filter(loggedUser,userIdVisit)
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }
}
