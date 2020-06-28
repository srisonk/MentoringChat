package com.example.mentoringchat

import android.app.ProgressDialog
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
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    /**
     * Declaring all of these globally so that could be accessed from all other fragments
     */

    var loggedUserId : String? = "one"
    var dbUsers : String? = "theUser"
    var userName: String? = "Username"
    var profile: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)

        loggedUserId = intent.getStringExtra("UserID")
        dbUsers = intent.getStringExtra("AllUsers")
        userName = intent.getStringExtra("UserName")
        profile = intent.getStringExtra("profile")


        user_name.text = userName
        if(profile != "" && profile != "null")
        {
            Picasso.get().load(profile).placeholder(R.drawable.profile).into(profile_image)
        }

        val toolbar : Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        val viewPager : ViewPager = findViewById(R.id.view_pager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        /**
         * Adding and displaying all the fragments from the MainActivity*/

        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        viewPagerAdapter.addFragment(SearchFragment(), "Contacts")
        viewPagerAdapter.addFragment(SettingsFragment(), "Profile")

        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

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
            fragments = ArrayList()
            titles = ArrayList()
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
