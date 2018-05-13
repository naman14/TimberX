package com.naman14.timberx.ui.songs

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.naman14.timberx.R

class SongsFragment : Fragment() {

    companion object {
        fun newInstance() = SongsFragment()
    }

    private lateinit var viewModel: SongsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)

        viewModel.getSongs(activity as Context)?.observe(this, Observer{ songs ->

        })
    }

}
