package com.khs.howstoday.fragment.bottom.talks


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.khs.howstoday.R

/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {

    var charFragment:View?=null

/*
    companion object {
        fun newInstance():ChatFragment{
            return ChatFragment()
        }
    }
*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Talks","ChatFragment() onCreateView()")
        charFragment = inflater.inflate(R.layout.fragment_chat, container, false)
        var message = arguments?.getString("EXTRA_MESSAGE")
        return charFragment
    }


}
