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

import android.animation.AnimatorInflater.loadStateListAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.naman14.timberx.R
import com.naman14.timberx.TimberMusicService.Companion.TYPE_ALL_ALBUMS
import com.naman14.timberx.TimberMusicService.Companion.TYPE_ALL_ARTISTS
import com.naman14.timberx.TimberMusicService.Companion.TYPE_ALL_FOLDERS
import com.naman14.timberx.TimberMusicService.Companion.TYPE_ALL_GENRES
import com.naman14.timberx.TimberMusicService.Companion.TYPE_ALL_PLAYLISTS
import com.naman14.timberx.TimberMusicService.Companion.TYPE_ALL_SONGS
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.ui.activities.MainActivity
import com.naman14.timberx.ui.dialogs.AboutDialog
import com.naman14.timberx.util.extensions.addFragment
import com.naman14.timberx.util.extensions.drawable
import kotlinx.android.synthetic.main.main_fragment.appBar
import kotlinx.android.synthetic.main.main_fragment.tabLayout
import kotlinx.android.synthetic.main.main_fragment.viewpager
import kotlinx.android.synthetic.main.toolbar.btnSearch
import kotlinx.android.synthetic.main.toolbar.mediaRouteButton
import kotlinx.android.synthetic.main.toolbar.toolbar

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 1
        tabLayout.setupWithViewPager(viewpager)

        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val animatorRes = if (verticalOffset == 0) {
                R.animator.appbar_elevation_disable
            } else {
                R.animator.appbar_elevation_enable
            }
            appBar.stateListAnimator = loadStateListAnimator(context, animatorRes)
        })

        val mainActivity = activity as MainActivity
        toolbar.overflowIcon = mainActivity.drawable(R.drawable.ic_more_vert_black_24dp)
        mainActivity.setSupportActionBar(toolbar)
        mainActivity.supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        btnSearch.setOnClickListener { activity.addFragment(SearchFragment()) }
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
                val context = activity ?: return false
                AboutDialog.show(context)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val res = context?.resources ?: return
        val adapter = Adapter(childFragmentManager).apply {
            addFragment(
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_SONGS.toString(), null)),
                    res.getString(R.string.songs)
            )
            addFragment(
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_ALBUMS.toString(), null)),
                    res.getString(R.string.albums)
            )
            addFragment(
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_PLAYLISTS.toString(), null)),
                    res.getString(R.string.playlists)
            )
            addFragment(
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_ARTISTS.toString(), null)),
                    res.getString(R.string.artists)
            )
            addFragment(
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_FOLDERS.toString(), null)),
                    res.getString(R.string.folders)
            )
            addFragment(
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_GENRES.toString(), null)),
                    res.getString(R.string.genres)
            )
        }
        viewPager.adapter = adapter
    }

    internal class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val fragments = ArrayList<Fragment>()
        private val titles = ArrayList<String>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        override fun getPageTitle(position: Int) = titles[position]
    }
}
