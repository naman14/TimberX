package com.naman14.timberx.util

import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naman14.timberx.R
import com.naman14.timberx.ui.widgets.PlayPauseButton
import com.squareup.picasso.Picasso

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, uri: String) {
    if (uri.isNotEmpty())
        Picasso.get().load(uri).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("playState")
fun setPlayState(view: PlayPauseButton, state: Int) {
    if (view.isPlayed != (state == PlaybackStateCompat.STATE_PLAYING )) {
        view.isPlayed = (state == PlaybackStateCompat.STATE_PLAYING)
        view.startAnimation()
    }
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
}