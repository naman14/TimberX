package com.naman14.timberx.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naman14.timberx.R
import com.squareup.picasso.Picasso

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
}