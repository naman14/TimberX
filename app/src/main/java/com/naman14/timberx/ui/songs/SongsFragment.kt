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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentSongsBinding;
import com.naman14.timberx.util.AutoClearedValue
import kotlinx.android.synthetic.main.fragment_songs.view.*


class SongsFragment : Fragment() {

    companion object {
        fun newInstance() = SongsFragment()
    }

    lateinit var viewModel: SongsViewModel

    var binding by AutoClearedValue<FragmentSongsBinding>(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_songs, container, false)

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = SongsAdapter()

        binding.root.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.root.recyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)

        viewModel.getSongs().observe(this, Observer{ songs ->
            adapter.updateData(songs!!)
        })
    }

}
