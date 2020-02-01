package com.khs.howstoday.fragment.bottom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.khs.howstoday.R
import com.khs.howstoday.action.talks.action.TalksFragmentAction
import com.khs.howstoday.fragment.bottom.talks.ChatFragment
import com.khs.howstoday.fragment.bottom.talks.FriendFragment
import com.khs.howstoday.fragment.bottom.talks.TalksTabInfo
import kotlinx.android.synthetic.main.fragment_talks.*

class TalksFragment: Fragment(){

    var fragmentView:View?=null                         // talks fragment 전체
    var talksFragmentAction:TalksFragmentAction?=null

    var tabLayout:TabLayout?=null                           // talks tab
    var viewPager:ViewPager?=null                           // 친구 혹은 대화 목록
    var viewPagerAdapter:FragmentStatePagerAdapter?=null    // 뷰페이저 어댑터


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_talks, container, false)
        initializeVariable()
        setupViewPagerAdapter()
        return fragmentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTalksFragmentAction()
    }

    private fun setTalksFragmentAction() {
        talksFragmentAction = TalksFragmentAction().apply {
            addTalksFragmentActionInterface(object : TalksFragmentAction.TalksFragmentActionInterface {
                override fun addFriend() {
                    var currentFragment = viewPagerAdapter?.getItem(viewPager!!.currentItem) // 현재 보고 있는 프래그먼트
                    if(currentFragment is FriendFragment) currentFragment.toggleSearchBar()             // 친구검색 영역 토글
                }
            })
        }
        setupListener()
    }


    private fun setupListener() {
        talks_btn_addfriend.setOnClickListener(talksFragmentAction) // 반드시 onActivityCreated 후에 호출
    }

    private fun initializeVariable() {
        tabLayout = fragmentView?.findViewById<TabLayout>(R.id.talks_tabs)
        viewPager = fragmentView?.findViewById<ViewPager>(R.id.talks_viewpager)
    }

    private fun setupViewPagerAdapter() {
        var adapter = TalksViewPagerAdapter(fragmentManager!!)
        adapter.addFragment(R.drawable.icon_tab_friends,FriendFragment(),"")
        adapter.addFragment(R.drawable.icon_tab_chat,ChatFragment(),"")
        viewPager?.adapter = adapter
        tabLayout?.setupWithViewPager(viewPager)
        setTabLayoutIcon(tabLayout,adapter)
        viewPagerAdapter = adapter
    }

    private fun setTabLayoutIcon(tabLayout: TabLayout?, adapter: TalksViewPagerAdapter){
        for(i in 0..viewPager?.adapter?.count!!)
            tabLayout?.getTabAt(i)?.setIcon(adapter!!.getFragmentInfo(i).iconResId!!)
    }

    inner class TalksViewPagerAdapter(fm: FragmentManager):FragmentStatePagerAdapter(fm,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        var mFragmentInfoList:ArrayList<TalksTabInfo> = arrayListOf()

        override fun getItem(position: Int): Fragment {
            return mFragmentInfoList[position].fragment!!
        }

        override fun getCount(): Int {
            return mFragmentInfoList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentInfoList[position].title
        }

        fun addFragment(iconResId:Int,fragment:Fragment,title:String){
            var newTab = TalksTabInfo(iconResId,title,fragment)
            mFragmentInfoList.add(newTab)
        }

        fun getFragmentInfo(position:Int):TalksTabInfo{
            return mFragmentInfoList[position]
        }
    }
}


