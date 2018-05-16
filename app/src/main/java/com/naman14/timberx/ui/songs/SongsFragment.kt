package com.naman14.timberx.ui.songs

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentSongsBinding;
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import kotlinx.android.synthetic.main.fragment_songs.*
import com.naman14.timberx.util.*


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

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)

        viewModel.getSongs().observe(this, Observer{ songs ->
            adapter.updateData(songs!!)
        })

        recyclerView.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {

                doAsync {
                    TimberDatabase.getInstance(activity!!)!!.queueDao().clearQueueSongs()
                    val queueEntity: QueueEntity = QueueEntity(0, position, 0,0,0)
                    TimberDatabase.getInstance(activity!!)!!.queueDao().insert(queueEntity)

                    val list: List<SongEntity> = adapter.songs!!.toSongEntityList()
                    TimberDatabase.getInstance(activity!!)!!.queueDao().insertAllSongs(list)
                }.execute()



            }
        })
    }

}
