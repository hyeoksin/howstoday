package com.khs.howstoday.action

import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.khs.howstoday.R


class BottomNavigation():BottomNavigationView.OnNavigationItemSelectedListener{

    public var navigationinterface:ActionBottomNavigation?= null

    interface ActionBottomNavigation{
        fun posts()
        fun rooms()
        fun talks()
        fun friends()
        fun profile()
    }

    fun addActionBottomNavigationInterface(listener: ActionBottomNavigation){
        navigationinterface = listener
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_posts ->{
                navigationinterface?.posts()
                return true
            }
            R.id.action_rooms ->{
                navigationinterface?.rooms()
                return true
            }
            R.id.action_talks ->{
                navigationinterface?.talks()
                return true
            }
            R.id.action_friends ->{
                navigationinterface?.friends()
                return true
            }
            R.id.action_profile ->{
                navigationinterface?.profile()
                return true
            }
        }
        return false
    }
}