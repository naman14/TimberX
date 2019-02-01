/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */

package com.naman14.timberx.ui.fragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.view.GravityCompat
import com.naman14.timberx.R
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.naman14.timberx.ui.activities.MainActivity
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.ui.dialogs.AboutDialog
import com.naman14.timberx.util.addFragment
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.toolbar.*


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
        setHasOptionsMenu(true)

        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 1

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
            setSupportActionBar(toolbar.apply { overflowIcon = ContextCompat.getDrawable(activity!!,
                    R.drawable.ic_more_vert_black_24dp) })
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(false)
            }
        }

        btnSearch.setOnClickListener {
            (activity as MainActivity).addFragment(SearchFragment())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setupCastButton(mediaRouteButton)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_about -> {
                AboutDialog().show(activity!!.supportFragmentManager, "AboutDialog")
            }
        }
        return super.onOptionsItemSelected(item)
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
