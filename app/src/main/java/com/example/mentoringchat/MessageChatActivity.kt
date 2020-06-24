package com.example.mentoringchat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.mentoringchat.AdapterClasses.ChatsAdapter
import com.example.mentoringchat.ModelClasses.Chat
import com.example.mentoringchat.ModelClasses.FileDataPart
import com.example.mentoringchat.ModelClasses.Users
import com.example.mentoringchat.ModelClasses.VolleyFileUploadRequest
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_asp.*
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
    var firebaseUser : FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_view_chats: RecyclerView
    var reference: DatabaseReference? = null

    var loggedUser: String = ""
    var allUsers: String = ""
    var userName: String = ""

    private var filePath : Uri? = null

    private var imageData: ByteArray? = null
    private val postURL: String = "https://ptsv2.com/t/54odo-1576291398/post" // remember to use your own api

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
            startActivity(intent)
            finish()
        }

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        loggedUser = intent.getStringExtra("user_id")
        allUsers = intent.getStringExtra("AllUsers")
        userName = intent.getStringExtra("UserName")
        //firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManger = LinearLayoutManager(applicationContext)
        linearLayoutManger.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManger

        Executors.newSingleThreadExecutor().execute{
            val receiverName = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$userIdVisit").readText()
            val answer = JSONArray(receiverName)
            val name:JSONObject = answer.get(0) as JSONObject
            val userName = name.get("username").toString()
            username_mchat.text = userName
        }

        retrieveMessages(loggedUser, userIdVisit, "https://firebasestorage.googleapis.com/v0/b/mentoringchat-a0917.appspot.com/o/profile.png?alt=media&token=eb81b28e-519a-4111-bc9e-0b5d81ff33a2")


//        reference = FirebaseDatabase.getInstance().reference
//            .child("Users").child(userIdVisit)
//        reference!!.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(p0: DataSnapshot)
//            {
//                /**********************UNCOMMENT THIS LATER*******************************/
//                /*
//                val user: Users? = p0.getValue(Users::class.java)
//
//                username_mchat.text = user!!.getUsername()
//                Picasso.get().load(user.getProfile()).into(profile_image_mChat)
//
//                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.getProfile())
//                */
//                /**********************UNCOMMENT ABOVE LATER*******************************/
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })

        send_message_btn.setOnClickListener {
            val message = text_message.text.toString()
            if (message == "")
            {
                Toast.makeText(this@MessageChatActivity, "Please write a message", Toast.LENGTH_LONG).show()
            }
            else
            {
                sendMessageToUser(loggedUser, userIdVisit, message)
                val chat = Chat()
                chat.setMessage(message)
                chat.setReceiver(userIdVisit)
                chat.setSender(loggedUser)
                (mChatList as ArrayList<Chat>).add(chat)
                filter(loggedUser,userIdVisit)
            }
            text_message.setText("")
        }

        attach_image_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select an image"), 438)
        }

        seenMessage(userIdVisit)
    }



    @SuppressLint("SimpleDateFormat")
    private fun sendMessageToUser(senderId: String?, receiverId: String?, message: String)
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val messageObject = JSONObject()
        messageObject.put("Id_sender",senderId!!.toInt())
        messageObject.put("Id_receiver",receiverId!!.toInt())
        messageObject.put("content",message)
        messageObject.put("TimeStamp",currentDate)

        val messageListObject = JSONObject()
        messageListObject.put("Id_sender",senderId!!.toInt())
        messageListObject.put("Id_receiver",receiverId!!.toInt())

        Executors.newSingleThreadExecutor().execute{
            post("https://mentoringacademyipb.azurewebsites.net/api/messages", messageObject.toString())
            post("https://mentoringacademyipb.azurewebsites.net/api/chatList", messageListObject.toString())
        }
        Thread.sleep(2500)
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

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data!= null && data!!.data != null){
            val progressBar =  ProgressDialog(this)
            progressBar.setMessage("The image is being uploaded, please hold on..")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(!task.isSuccessful)
                {
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val currentDate = sdf.format(Date())
                    val messageObject = JSONObject()
                    messageObject.put("Id_sender",loggedUser!!.toInt())
                    messageObject.put("Id_receiver",userIdVisit!!.toInt())
                    messageObject.put("content","sent you an image.")
                    messageObject.put("TimeStamp",currentDate)
                    //messageObject.put("url",url)

                    Executors.newSingleThreadExecutor().execute{
                        post("https://mentoringacademyipb.azurewebsites.net/api/messages", messageObject.toString())
                        progressBar.dismiss()
                    }
                    Thread.sleep(2500)

//                    val downloadUrl = task.result
//                    val url = downloadUrl.toString()
//
//                    val messageHashMap = HashMap<String, Any?>()
//                    messageHashMap["sender"] = firebaseUser!!.uid
//                    messageHashMap["message"] = "sent you an image."
//                    messageHashMap["receiver"] = userIdVisit
//                    messageHashMap["isseen"] = false
//                    messageHashMap["url"] = url
//                    messageHashMap["messageId"] = messageId
//
//                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)

//                    progressBar.dismiss()
                }
            }
        }
    }*/

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
                //contactList.add(jsonObj.get("username").toString())

                if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId) || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                {
                    (mChatList as ArrayList<Chat>).add(chat)
                    chatsAdapter!!.notifyDataSetChanged()
                }
                chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), loggedUser)
                recycler_view_chats.adapter = chatsAdapter

                //chatsAdapter = ChatsAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
            }
        }
        Thread.sleep(2500)
    }

    private fun filter(senderId: String, receiverId: String?){
        val myList = ArrayList<Chat>()

        for(s in (mChatList as ArrayList<Chat>)){
            if(s!!.getReceiver().equals(senderId) && s.getSender().equals(receiverId) || s.getReceiver().equals(receiverId) && s.getSender().equals(senderId)){
                myList.add(s)
            }
        }
        chatsAdapter = ChatsAdapter(this@MessageChatActivity, myList, loggedUser)
        recycler_view_chats.adapter = chatsAdapter
    }


    var seenListener: ValueEventListener? = null


    private fun seenMessage(userId: String)
    {
        val chat = Chat()

        if(chat!!.getReceiver().equals(loggedUser) && chat!!.getSender().equals(userId))
        {
            chat.setIsSeen(true)
        }

//        seenListener = reference.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(p0: DataSnapshot) {
//                for(dataSnapshot in p0.children)
//                {
//                    val chat = dataSnapshot.getValue(Chat::class.java)
//                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId))
//                    {
//                        val hashMap = HashMap<String, Any>()
//                        hashMap["isseen"] = true
//                        dataSnapshot.ref.updateChildren(hashMap)
//                    }
//                }
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })
    }

    private fun uploadImage() {
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
                params["imageFile"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val uri = data?.data
            if (uri != null) {
                imageView.setImageURI(uri)
                createImageData(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
