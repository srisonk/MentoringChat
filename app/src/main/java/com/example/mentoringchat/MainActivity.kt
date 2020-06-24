package com.example.mentoringchat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.mentoringchat.Fragments.ChatsFragment
import com.example.mentoringchat.Fragments.SearchFragment
import com.example.mentoringchat.Fragments.SettingsFragment
import com.example.mentoringchat.ModelClasses.Chat
import com.example.mentoringchat.ModelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var userDetails = "start"
    private var jsonObject : JSONObject? = null

//    var refUsers: DatabaseReference? = null
//    var firebaseUser: FirebaseUser? = null

    //Declaring all of these 3 globally so that could be accessed from all other activities

    var loggedUserId : String? = "one"
    var dbUsers : String? = "theUser"
    var userName: String? = "Username"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)

        loggedUserId = intent.getStringExtra("UserID")
        dbUsers = intent.getStringExtra("AllUsers")
        userName = intent.getStringExtra("UserName")


        user_name.text = userName

        //getUserDetails()

        //user_name.text = userDetails


        //firebaseUser = FirebaseAuth.getInstance().currentUser
        //refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        val viewPager : ViewPager = findViewById(R.id.view_pager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        viewPagerAdapter.addFragment(SearchFragment(), "Contacts")
        viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)




        /*val ref = FirebaseDatabase.getInstance().reference.child("Chats")


        ref!!.addValueEventListener(object : ValueEventListener{
            //Focus only on the part lower for changes
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0

                for (dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.isIsSeen()){
                        countUnreadMessages += 1
                    }
                }

                if(countUnreadMessages == 0)
                {
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
                }
                else
                {
                    viewPagerAdapter.addFragment(ChatsFragment(), "($countUnreadMessages) Chats")
                }

                viewPagerAdapter.addFragment(SearchFragment(), "Contacts")
                viewPagerAdapter.addFragment(SettingsFragment(), "Settings")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })*/
    }

    private fun chatList(){
        val toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        val viewPager : ViewPager = findViewById(R.id.view_pager)

    }

    private fun getUserDetails(): String{
        Executors.newSingleThreadExecutor().execute {
            //userDetails = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$loggedUserId").readText()
            userDetails = URL("https://mentoringacademyipb.azurewebsites.net/api/users/$loggedUserId").readText()
            val value = """$userDetails"""
            val answer = JSONArray(value)

            val one:JSONObject = answer.get(0) as JSONObject

            userDetails = one.get("username").toString()
        }
        return userDetails
    }

    //Completed using API Request
    /*refUsers!!.addValueEventListener(object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists())
            {
                val user: Users? = p0.getValue(Users::class.java)

                user_name.text = user!!.getUsername()
                Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(profile_image)
            }
        }

    })*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId)
        {
            R.id.action_logout ->
            {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }
        }
        return false
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager){

        private val fragments : ArrayList<Fragment>
        private val titles : ArrayList<String>

        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()
        }
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment : Fragment, title: String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }

    }
}
