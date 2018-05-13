package com.naman14.timberx.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naman14.timberx.R
import android.R.id.tabs
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import java.nio.file.Files.size
import androidx.fragment.app.FragmentPagerAdapter
import com.naman14.timberx.AlbumDetailFragment


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.main_fragment, container, false)

        val viewPager = v.findViewById(R.id.viewpager) as ViewPager
        setupViewPager(viewPager)
        viewPager.offscreenPageLimit = 2

        val tabLayout = v.findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setupWithViewPager(viewPager)

        return v;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(childFragmentManager)
        adapter.addFragment(AlbumDetailFragment.newInstance(), this.getString(R.string.songs))
        adapter.addFragment(AlbumDetailFragment.newInstance(), this.getString(R.string.albums))
        adapter.addFragment(AlbumDetailFragment.newInstance(), this.getString(R.string.artists))
        adapter.addFragment(AlbumDetailFragment.newInstance(), this.getString(R.string.genres))
        adapter.addFragment(AlbumDetailFragment.newInstance(), this.getString(R.string.folders))
        adapter.addFragment(AlbumDetailFragment.newInstance(), this.getString(R.string.playlists))
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
