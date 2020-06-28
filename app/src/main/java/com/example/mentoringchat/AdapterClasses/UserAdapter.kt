package com.example.mentoringchat.AdapterClasses

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mentoringchat.MainActivity
import com.example.mentoringchat.MessageChatActivity
import com.example.mentoringchat.ModelClasses.Chat
import com.example.mentoringchat.ModelClasses.Chatlist
import com.example.mentoringchat.ModelClasses.Users
import com.example.mentoringchat.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

// Initializing and declaring the constructors as per the requirement
class UserAdapter(mContext: Context,
                  mUsers: List<Users>,
                  isChatCheck: Boolean,
                  currentUser: String,
                  allUsers: String,
                  userName: String,
                  profile: String
                ):RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
    private val mContext:Context
    private val mUsers:List<Users> = mUsers
    private val isChatCheck: Boolean
    private var currentUser:String
    private val allUsers:String
    private val userName:String
    private val profile:String
    var lastMsg: String = ""
    private var messageGet = ""


    init {
        this.mContext = mContext
        this.isChatCheck = isChatCheck
        this.currentUser = currentUser
        this.allUsers = allUsers
        this.userName = userName
        this.profile = profile
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int)
    {
        val user = mUsers[i]

        // Verifies if the User model class contains the image on his profile.. If contains, displays on top alongside the username - Receiver side
        holder.userNameTxt.text = user.getUsername()
        if(user.getProfile() != "null" && user.getProfile() != "")
        {
            Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)
        }

        // isChatCheck's value is provided from the fragments while calling this adapter. It is used to get the last message on ChatsFragment
        if(isChatCheck)
        {
            retrieveLastMessage(user.getUID(), holder.lastMessageTxt)
        }
        else
        {
            holder.lastMessageTxt.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user.getUID())
            intent.putExtra("user_id",currentUser)
            intent.putExtra("AllUsers",allUsers)
            intent.putExtra("UserName",userName)
            intent.putExtra("profile",profile)
            //intent.putExtra("visit_id",ACTIVITY.loggedUserId.toString())
            mContext.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var userNameTxt: TextView
        var profileImageView: CircleImageView
        var onlineImageView: CircleImageView
        var offlineImageView: CircleImageView
        var lastMessageTxt: TextView

        init{
            userNameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)
        }
    }

    // This function retrieves the last message between the chat of users.
    @SuppressLint("SetTextI18n")
    private fun retrieveLastMessage(chatUserId: String?, lastMessageTxt: TextView)
    {
        lastMsg = "defaultMsg"
        getDatabaseContent()

        // Making the thread sleep so that another thread will go to network and get contents from database
        Thread.sleep(1500)

        val allChatList = JSONArray(messageGet)

        // An iterator to create each message as JSON object then store the key-value of JSON object to chat model class.

        for(i in 0 until allChatList.length())
        {
            val jsonObj: JSONObject = allChatList.getJSONObject(i)
            val chat = Chat()

            chat.setMessageId(jsonObj.get("msg_id").toString())
            chat.setSender(jsonObj.get("id_sender").toString())
            chat.setReceiver(jsonObj.get("id_receiver").toString())
            chat.setMessage(jsonObj.get("content").toString())

            // Making the verification if the sender and receiver matches.
            if(chat.getReceiver() == currentUser &&
                chat.getSender() == chatUserId ||
                chat.getReceiver() == chatUserId &&
                chat.getSender() == currentUser)
            {
                lastMsg = chat.getMessage().toString()
            }
        }
        // As long as condition is true, will get the updated value of last message from previous if statement.
        when(lastMsg)
        {
            "defaultMsg" -> lastMessageTxt.text = "No message"
            else -> lastMessageTxt.text = lastMsg
        }
        lastMsg = "defaultMsg"
    }


    // Makes request in the database to get the content of the all messages from the database.
    private fun getDatabaseContent()
    {
        Executors.newSingleThreadExecutor().execute {
            messageGet = URL("https://mentoringacademyipb.azurewebsites.net/api/messages/").readText()
        }
    }

}