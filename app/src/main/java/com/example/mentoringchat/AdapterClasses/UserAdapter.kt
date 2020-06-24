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
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

class UserAdapter(mContext: Context,
                  mUsers: List<Users>,
                  isChatCheck: Boolean,
                  currentUser: String,
                  allUsers: String,
                  userName: String
                ):RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
    private val mContext:Context
    private val mUsers:List<Users> = mUsers
    private val isChatCheck: Boolean
    private var currentUser:String
    private val allUsers:String
    private val userName:String
    var lastMsg: String = ""
    private var messageGet = ""


    init {
        this.mContext = mContext
        this.isChatCheck = isChatCheck
        this.currentUser = currentUser
        this.allUsers = allUsers
        this.userName = userName
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
        //val user: Users? = mUsers[i]
        val user = mUsers[i]

        holder.userNameTxt.text = user.getUsername()
        //Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)

        if(isChatCheck)
        {
            retrieveLastMessage(user.getUID(), holder.lastMessageTxt)
        }
        else
        {
            holder.lastMessageTxt.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "View Profile"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("Select an option")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if(position == 0)
                {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    intent.putExtra("user_id",currentUser)
                    intent.putExtra("AllUsers",allUsers)
                    intent.putExtra("UserName",userName)
                    //intent.putExtra("visit_id",ACTIVITY.loggedUserId.toString())
                    mContext.startActivity(intent)
                }
                if(position == 1)
                {

                }
            })
            builder.show()
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

    @SuppressLint("SetTextI18n")
    private fun retrieveLastMessage(chatUserId: String?, lastMessageTxt: TextView)
    {
        lastMsg = "defaultMsg"
        getDatabaseContent()

        Thread.sleep(2500)

        val allChatList = JSONArray(messageGet)

        for(i in 0 until allChatList.length())
        {
            val jsonObj: JSONObject = allChatList.getJSONObject(i)
            val chat = Chat()

            chat.setMessageId(jsonObj.get("msg_id").toString())
            chat.setSender(jsonObj.get("id_sender").toString())
            chat.setReceiver(jsonObj.get("id_receiver").toString())
            chat.setMessage(jsonObj.get("content").toString())

            if(chat.getReceiver() == currentUser &&
                chat.getSender() == chatUserId ||
                chat.getReceiver() == chatUserId &&
                chat.getSender() == currentUser)
            {
                lastMsg = chat.getMessage().toString()
            }
        }
        when(lastMsg)
        {
            "defaultMsg" -> lastMessageTxt.text = "No message"
            else -> lastMessageTxt.text = lastMsg
        }
        lastMsg = "defaultMsg"
    }

    private fun getDatabaseContent()
    {
        Executors.newSingleThreadExecutor().execute {
            messageGet = URL("https://mentoringacademyipb.azurewebsites.net/api/messages/").readText()
        }
    }

}