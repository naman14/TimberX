package com.naman14.timberx.ui.fragments

import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import com.naman14.timberx.R
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.naman14.timberx.ui.activities.MainActivity
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.util.MediaID
import kotlinx.android.synthetic.main.main_activity.*
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

        (activity as MainActivity).apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_menu_black)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).drawerLayout
                        .openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(childFragmentManager)
        adapter.addFragment(MediaItemFragment.newInstance(MediaID(TimberMusicService.TYPE_ALL_SONGS.toString(), null)), this.getString(R.string.songs))
        adapter.addFragment(MediaItemFragment.newInstance(MediaID(TimberMusicService.TYPE_ALL_ALBUMS.toString(), null)), this.getString(R.string.albums))
        adapter.addFragment(MediaItemFragment.newInstance(MediaID(TimberMusicService.TYPE_ALL_PLAYLISTS.toString(), null)), this.getString(R.string.playlists))
        adapter.addFragment(MediaItemFragment.newInstance(MediaID(TimberMusicService.TYPE_ALL_ARTISTS.toString(), null)), this.getString(R.string.artists))
        adapter.addFragment(MediaItemFragment.newInstance(MediaID(TimberMusicService.TYPE_ALL_FOLDERS.toString(), null)), this.getString(R.string.folders))
        adapter.addFragment(MediaItemFragment.newInstance(MediaID(TimberMusicService.TYPE_ALL_GENRES.toString(), null)), this.getString(R.string.genres))
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
