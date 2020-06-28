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
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

/**
 * This fragment is responsible to display the users and last messages when logged user initiates the chat
 */
class ChatsFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<Chatlist>? = null
    lateinit var recycler_view_chatlist : RecyclerView

    lateinit var ACTIVITY: MainActivity

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

        usersChatList = ArrayList()

        getDatabaseContent()
        Thread.sleep(2800)

        /**
         * The messageList string received from getDatabaseContent() is converted into JSON array
         * Each content of array is converted to individual JSON object
         * JSON objects are stored in the chatList model class
         * chatList model class has been initialized inside the loop so that it will reset its value and expect the new value every time we iterate the array
         * chatList model class stores all the array value into itself*/

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

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        ACTIVITY = context as MainActivity
    }

    /**
     * The lower function is used to retrieve the chatList for the user that has logged in
     * myval gets the value of all users in the database from the call in private function getDatabaseContent()
     * users are converted to JSON array
     * JSON array of users are converted to JSON object and iterated individually
     * all Users are stored to Users Model class
     * later the verification is made is the user that is stored to model class is the receiver in the chat list, that user is added to the mUsers ArrayList
     * mUsers will contain the users that are receiver.
     * mUsers will be passed to userAdapter in order to display them in the list in the UI*/

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
            user.setProfile(jsonObj.get("profile").toString())
            user.setCover(jsonObj.get("cover").toString())

            for(eachChatList in usersChatList!!)
            {
                if(user.getUID().toString() == eachChatList.getReceiver())
                {
                    (mUsers as ArrayList).add(user)
                }
            }
        }
        // Here isChatCheck is supplied as true in order to view the last text message in this Fragment below the user name.
        userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true, ACTIVITY.loggedUserId.toString(),
            ACTIVITY.dbUsers.toString(), ACTIVITY.userName.toString(), ACTIVITY.profile.toString())
        recycler_view_chatlist.adapter = userAdapter
    }

    /**
     * This function makes the GET request in the database to the chatList
     * chatList can be retrieved as per the userId.
     * When the userID is provided, it will return the unique chatList for that sender.
     * Second GET request is to retrieve all the users from the database.*/
    private fun getDatabaseContent()
    {
        Executors.newSingleThreadExecutor().execute {
            messageListRetrieved = URL("https://mentoringacademyipb.azurewebsites.net/api/chatList/${ACTIVITY.loggedUserId}").readText()
            userRetrieved = URL("https://mentoringacademyipb.azurewebsites.net/api/users/").readText()
        }
    }
}
