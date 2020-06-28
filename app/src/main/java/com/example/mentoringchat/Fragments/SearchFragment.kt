package com.example.mentoringchat.Fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mentoringchat.AdapterClasses.UserAdapter
import com.example.mentoringchat.MainActivity
import com.example.mentoringchat.ModelClasses.Users

import com.example.mentoringchat.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * This fragment is responsible to display all the users in the database,
 * Allows the user to search a particular user in the database,
 * Send message to a selected user.
 */
class SearchFragment : Fragment(){
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null

    lateinit var ACTIVITY: MainActivity

    private var myUID: String? = "0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View =  inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.SeachUsersET)


        mUsers = ArrayList()
        retrieveAllUsers()

        searchEditText!!.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {

            }

            // When user types something in the search bar, filter function is called.
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })

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

    /**
     * The lower function gets all the user from the MainActivity and store them in allUsers local val
     * the string of all users is converted to JSON array
     * Each element of array is iterated and converted to JSON object
     * Key-value pair from JSON object is extracted and stored to User Model class.
     * The list of user is passed to UserAdapter in order to display to the UI.
     * */
    private fun retrieveAllUsers()
    {
        if (searchEditText!!.text.toString() == "") {
            val allUsers = JSONArray(ACTIVITY.dbUsers)

            for (i in 0 until allUsers.length()) {
                val jsonObj: JSONObject = allUsers.getJSONObject(i)
                val user = Users()
                if ((ACTIVITY.loggedUserId)!!.toInt() != jsonObj.get("user_id")) {
                    user.setUID(jsonObj.get("user_id").toString())
                    user.setUsername(jsonObj.get("username").toString())
                    user.setGender(jsonObj.get("gender").toString())
                    user.setNationality(jsonObj.get("nationality").toString())
                    user.setPassword(jsonObj.get("password").toString())
                    user.setBirthdate(jsonObj.get("birthdate").toString())
                    user.setCourseId(jsonObj.get("course_id").toString())
                    user.setProfile(jsonObj.get("profile").toString())
                    user.setCover(jsonObj.get("cover").toString())
                    (mUsers as ArrayList<Users>).add(user)
                }
            }
            // isChatCheck is set to false because here in contact list we don't want to display the last message if exist between users.
            userAdapter = UserAdapter(context!!, mUsers!!, false, ACTIVITY.loggedUserId.toString(), ACTIVITY.dbUsers.toString(), ACTIVITY.userName.toString(), ACTIVITY.profile.toString())
            recyclerView!!.adapter = userAdapter
        }

    }

    /**
     * The lower function first gets all the users, stores them in ArrayList of type Users
     * The text that user types is converted to lowercase and then it checks if,
     * the character that user typed matches any character in the list of username
     * when match is found, a new list is created and that new list is passed to userAdapter to display the names*/
    private fun filter(text: String){
        val myList = ArrayList<Users>()

        for(s in (mUsers as ArrayList<Users>)){
            if(s.getUsername()!!.toLowerCase().contains(text.toLowerCase())){
                myList.add(s)
            }
        }
        userAdapter = UserAdapter(context!!, myList.toList(), false, ACTIVITY.loggedUserId.toString(), ACTIVITY.dbUsers.toString(),
            ACTIVITY.userName.toString(), ACTIVITY.profile.toString())
        recyclerView!!.adapter = userAdapter
    }
}
