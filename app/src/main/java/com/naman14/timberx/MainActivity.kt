package com.naman14.timberx

import android.os.Bundle
import android.widget.RelativeLayout
import com.naman14.timberx.ui.main.MainFragment
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.databinding.MainActivityBinding
import com.naman14.timberx.util.replaceFragment

class MainActivity : MediaBrowserActivity() {

    private lateinit var viewModel: MainViewModel
    private var binding: MainActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            replaceFragment(MainFragment.newInstance())
        }

        setupUI()
    }


    private fun setupUI() {

        val layoutParams = progressBar.layoutParams as RelativeLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams

        binding?.let {
            it.viewModel = viewModel
            it.setLifecycleOwner(this)
        }
//
//        val mUpdateProgress = object : Runnable {
//            override fun run() {
//                val playing = getService()?.isPlaying ?: false
//                if (playing) {
//                    val position = getService()?.position()
//                    viewModel.progressLiveData.postValue(position)
//                    progressBar.postDelayed(this, 10)
//                } else progressBar.removeCallbacks(this)
//            }
//        }
//
//        viewModel.addObservers().observe(this, Observer {
//            progressBar.postDelayed(mUpdateProgress, 10)
//        })
    }

}
