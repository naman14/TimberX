package com.naman14.timberx.util

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naman14.timberx.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

object ImageTransformation {
    private val radius = 2f //in dp
    private val margin = 0
    fun transformation(context: Context): Transformation = RoundedCornersTransformation(Utils.convertDpToPixel(radius, context).toInt(), margin)
}

object LargeImageTransformation {

    private val radius = 5f //in dp
    private val margin = 0

    fun transformation(context: Context): Transformation = RoundedCornersTransformation(Utils.convertDpToPixel(radius, context).toInt(), margin)
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).centerCrop().resizeDimen(R.dimen.album_art, R.dimen.album_art).transform(ImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrlLarge")
fun setImageUrlLarge(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).centerCrop().resizeDimen(R.dimen.album_art_large, R.dimen.album_art_large).transform(LargeImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrlNormal")
fun setImageUrlNormal(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).transform(ImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, uri: String) {
    if (uri.isNotEmpty())
        Picasso.get().load(uri).centerCrop().resizeDimen(R.dimen.album_art, R.dimen.album_art).transform(ImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("playState")
fun setPlayState(view: ImageView, state: Int) {
    if (state == PlaybackStateCompat.STATE_PLAYING) {
        view.setImageResource(R.drawable.ic_pause_outline)
    } else {
        view.setImageResource(R.drawable.ic_play_outline)
    }
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
}