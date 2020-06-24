package com.example.mentoringchat.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mentoringchat.AdapterClasses.UserAdapter
import com.example.mentoringchat.MainActivity
import com.example.mentoringchat.ModelClasses.Chatlist
import com.example.mentoringchat.ModelClasses.Users

import com.example.mentoringchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<Chatlist>? = null
    lateinit var recycler_view_chatlist : RecyclerView
    private var firebaseUser: FirebaseUser? = null

    lateinit var ACTIVITY: MainActivity
    private var myUID: String = "7"

    private var userRetrieved = ""
    private var messageListRetrieved: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler_view_chatlist = view.findViewById(R.id.recycler_view_chatlist)
        recycler_view_chatlist.setHasFixedSize(true)
        recycler_view_chatlist.layoutManager = LinearLayoutManager(context)

        //firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        getDatabaseContent()
        Thread.sleep(4500)

        val allChatList = JSONArray(messageListRetrieved)

        for(i in 0 until allChatList.length()){
            val jsonObj: JSONObject = allChatList.getJSONObject(i)
            val chatList = Chatlist()
            chatList.setId(jsonObj.get("cL_id").toString())
            chatList.setSender(jsonObj.get("id_sender").toString())
            chatList.setReceiver(jsonObj.get("id_receiver").toString())
            (usersChatList as ArrayList).add(chatList)
        }
        retrieveChatLists()

//        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
//        ref!!.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(p0: DataSnapshot) {
//                (usersChatList as ArrayList).clear()
//
//                for(dataSnapshot in p0.children)
//                {
//                    val chatList = dataSnapshot.getValue(Chatlist::class.java)
//
//                    (usersChatList as ArrayList).add(chatList!!)
//                }
//                retrieveChatLists()
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        ACTIVITY = context as MainActivity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myUID = ACTIVITY.loggedUserId.toString()
    }

    private fun retrieveChatLists()
    {
        mUsers = ArrayList()

        val myval = userRetrieved
        val allUsers = JSONArray(myval)

        for(i in 0 until allUsers.length())
        {
            val jsonObj: JSONObject = allUsers.getJSONObject(i)
            val user = Users()
            user.setUID(jsonObj.get("user_id").toString())
            user.setUsername(jsonObj.get("username").toString())
            user.setGender(jsonObj.get("gender").toString())
            user.setNationality(jsonObj.get("nationality").toString())
            user.setPassword(jsonObj.get("password").toString())
            user.setBirthdate(jsonObj.get("birthdate").toString())
            user.setCourseId(jsonObj.get("course_id").toString())

            for(eachChatList in usersChatList!!)
            {
                if(user.getUID().toString() == eachChatList.getReceiver())
                {
                    (mUsers as ArrayList).add(user)
                }
            }
        }
        userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true, ACTIVITY.loggedUserId.toString(), ACTIVITY.dbUsers.toString(), ACTIVITY.userName.toString())
        recycler_view_chatlist.adapter = userAdapter



//        val ref = FirebaseDatabase.getInstance().reference.child("Users")
//        ref!!.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(p0: DataSnapshot) {
//                (mUsers as ArrayList).clear()
//
//                for(dataSnapshot in p0.children)
//                {
//                    val user = dataSnapshot.getValue(Users::class.java)
//
//                    for(eachChatList in usersChatList!!)
//                    {
//                        if(user!!.getUID().equals(eachChatList.getId())){
//                            (mUsers as ArrayList).add(user!!)
//                        }
//                    }
//                }
//                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true, ACTIVITY.loggedUserId.toString())
//                recycler_view_chatlist.adapter = userAdapter
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        })
    }

    private fun getDatabaseContent()
    {
        Executors.newSingleThreadExecutor().execute {
            messageListRetrieved = URL("https://mentoringacademyipb.azurewebsites.net/api/chatList/${ACTIVITY.loggedUserId}").readText()
            userRetrieved = URL("https://mentoringacademyipb.azurewebsites.net/api/users/").readText()
        }
    }


}
