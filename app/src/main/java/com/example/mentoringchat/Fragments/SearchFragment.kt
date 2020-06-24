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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

class SearchFragment : Fragment(){
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null

    private var song: String? = null

    var contactList = mutableListOf<String>()

    /***************************************************************************************************************/

    lateinit var ACTIVITY: MainActivity

    private var myUID: String? = "0"

    /***************************************************************************************************************/

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
                //searchForUsers(cs.toString().toLowerCase())
            }

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

    private fun retrieveAllUsers()
    {
//        if (searchEditText!!.text.toString() == ""){
//            var myval : String = "[{\"user_id\":3,\"username\":\"propername\",\"gender\":\"Female\",\"nationality\":\"Spanish\",\"password\":\"fml\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":4,\"username\":\"admin\",\"gender\":\"Male\",\"nationality\":\"French\",\"password\":\"admin\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":5,\"username\":\"John\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":6,\"username\":\"Mark\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":7,\"username\":\"Max\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":8,\"username\":\"Andrew\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":13,\"username\":\"Lockwood\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":15,\"username\":\"pog\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":17,\"username\":\"teste1username\",\"gender\":\"Female\",\"nationality\":\"Portuguese\",\"password\":\"teste1password\",\"birthdate\":\"26-09-1998\",\"course_id\":1},{\"user_id\":24,\"username\":\"WebTest\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":27,\"username\":\"usernameprpoper\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1}]"
//            val allUsers = JSONArray(myval)
//
//
//            for(i in 0 until allUsers.length()){
//                val jsonObj : JSONObject = allUsers.getJSONObject(i)
//                if(!(ACTIVITY.loggedUserId)!!.toInt().equals(jsonObj.get("user_id")))
//                {
//                    contactList.add(jsonObj.get("username").toString())
//                }
//            }
//
//            userAdapter = UserAdapter(context!!, contactList.toList()!!, false)
//            recyclerView!!.adapter = userAdapter
//        }

        if (searchEditText!!.text.toString() == "") {
//            val myval =
//                "[{\"user_id\":3,\"username\":\"propername\",\"gender\":\"Female\",\"nationality\":\"Spanish\",\"password\":\"fml\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":4,\"username\":\"admin\",\"gender\":\"Male\",\"nationality\":\"French\",\"password\":\"admin\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":5,\"username\":\"John\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":6,\"username\":\"Mark\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":7,\"username\":\"Max\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":8,\"username\":\"Andrew\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":13,\"username\":\"Lockwood\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":15,\"username\":\"pog\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":17,\"username\":\"teste1username\",\"gender\":\"Female\",\"nationality\":\"Portuguese\",\"password\":\"teste1password\",\"birthdate\":\"26-09-1998\",\"course_id\":1},{\"user_id\":24,\"username\":\"WebTest\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":27,\"username\":\"usernameprpoper\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1}]"
//            val allUsers = JSONArray(myval)
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
                    //contactList.add(jsonObj.get("username").toString())
                    (mUsers as ArrayList<Users>).add(user)
                }
            }
            userAdapter = UserAdapter(context!!, mUsers!!, false, ACTIVITY.loggedUserId.toString(), ACTIVITY.dbUsers.toString(), ACTIVITY.userName.toString())
            //userAdapter = UserAdapter(context!!, contactList.toList()!!, false)
            recyclerView!!.adapter = userAdapter
        }

        /********************************CP2***********************************/


        /*Executors.newSingleThreadExecutor().execute {
            //userDetails = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$loggedUserId").readText()
            userDetails = URL("http://10.0.2.2:5000/api/users/").readText()
            val allUsers = JSONArray(userDetails)

            for(i in 0 until allUsers.length()){
                val jsonObj : JSONObject = allUsers.get(i) as JSONObject
                if(!(ACTIVITY.loggedUserId)!!.equals(jsonObj.get("user_id"))){
                    userList!!.add(jsonObj.get("username").toString())
                }
            }
        }

        Thread.sleep(15000)*/

        /*******************************CP2************************************/


        /*refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot)
            {
                (mUsers as ArrayList<Users>).clear()
                if (searchEditText!!.text.toString() == "")
                {
                    for(snapshot in p0.children)
                    {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        if (!(user!!.getUID()).equals(firebaseUserID))
                        {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }
                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    recyclerView!!.adapter = userAdapter
                }
            }

        })*/

    }

//    private fun filter(text: String){
//        var searchList = mutableListOf<String>()
//
//        for(s in contactList){
//            if(s.toLowerCase().contains(text.toLowerCase())){
//                searchList.add(s)
//            }
//        }
//        userAdapter = UserAdapter(context!!, searchList.toList()!!, false)
//        recyclerView!!.adapter = userAdapter
//    }

    private fun filter(text: String){
        val myList = ArrayList<Users>()

        for(s in (mUsers as ArrayList<Users>)){
            if(s.getUsername()!!.toLowerCase().contains(text.toLowerCase())){
                myList.add(s)
            }
        }
        userAdapter = UserAdapter(context!!, myList.toList(), false, ACTIVITY.loggedUserId.toString(), ACTIVITY.dbUsers.toString(), ACTIVITY.userName.toString())
        recyclerView!!.adapter = userAdapter

//        var searchList = mutableListOf<String>()
//
//        for(s in contactList){
//            if(s.toLowerCase().contains(text.toLowerCase())){
//                searchList.add(s)
//            }
//        }
//        userAdapter = UserAdapter(context!!, searchList.toList()!!, false)
//        recyclerView!!.adapter = userAdapter
    }




    private fun searchForUsers(str: String)
    {
//        if (searchEditText!!.text.toString() == ""){
//            var myval : String = "[{\"user_id\":3,\"username\":\"propername\",\"gender\":\"Female\",\"nationality\":\"Spanish\",\"password\":\"fml\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":4,\"username\":\"admin\",\"gender\":\"Male\",\"nationality\":\"French\",\"password\":\"admin\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":5,\"username\":\"John\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":6,\"username\":\"Mark\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":7,\"username\":\"Max\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":8,\"username\":\"Andrew\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":13,\"username\":\"Lockwood\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"doe\",\"birthdate\":\"1984-03-15\",\"course_id\":1},{\"user_id\":15,\"username\":\"pog\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":17,\"username\":\"teste1username\",\"gender\":\"Female\",\"nationality\":\"Portuguese\",\"password\":\"teste1password\",\"birthdate\":\"26-09-1998\",\"course_id\":1},{\"user_id\":24,\"username\":\"WebTest\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1},{\"user_id\":27,\"username\":\"usernameprpoper\",\"gender\":\"Male\",\"nationality\":\"American\",\"password\":\"pog\",\"birthdate\":\"16-02-1997\",\"course_id\":1}]"
//            val allUsers = JSONArray(myval)
//
//
//            for(i in 0 until allUsers.length()){
//                val jsonObj : JSONObject = allUsers.getJSONObject(i)
//                if(!(ACTIVITY.loggedUserId)!!.toInt().equals(jsonObj.get("user_id")))
//                {
//                    contactList.add(jsonObj.get("username").toString())
//                }
//            }
//
//            userAdapter = UserAdapter(context!!, contactList.toList()!!, false)
//            recyclerView!!.adapter = userAdapter
//        }



        /*
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for(snapshot in p0.children)
                {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (!(user!!.getUID()).equals(firebaseUserID))
                    {
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = userAdapter
            }

        })*/
    }
}
