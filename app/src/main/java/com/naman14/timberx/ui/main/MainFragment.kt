package com.naman14.timberx.ui.main

import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naman14.timberx.R
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.naman14.timberx.ui.albumdetail.AlbumDetailFragment
import com.naman14.timberx.ui.albums.AlbumsFragment
import com.naman14.timberx.ui.songs.SongsFragment
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 2

        tabLayout.setupWithViewPager(viewpager)

        appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(p0: AppBarLayout?, p1: Int) {
                if (p1 == 0) {
                    appBar.stateListAnimator =  AnimatorInflater.loadStateListAnimator(context, R.animator.appbar_elevation_disable)
                } else{
                    appBar.stateListAnimator =  AnimatorInflater.loadStateListAnimator(context, R.animator.appbar_elevation_enable)
                }
            }
        })

    }


    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(childFragmentManager)
        adapter.addFragment(SongsFragment.newInstance(), this.getString(R.string.songs))
        adapter.addFragment(AlbumsFragment.newInstance(), this.getString(R.string.albums))
        viewPager.adapter = adapter
    }

    internal class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val mFragments = ArrayList<Fragment>()
        private val mFragmentTitles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            mFragments.add(fragment)
            mFragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitles[position]
        }
    }
}
