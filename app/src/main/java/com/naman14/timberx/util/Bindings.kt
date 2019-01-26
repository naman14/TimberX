package com.naman14.timberx.util

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.lastfm.DataHandler
import com.naman14.timberx.lastfm.Outcome
import com.naman14.timberx.lastfm.models.ArtistInfo
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
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).error(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, uri: String?) {
    if (uri != null && uri.isNotEmpty())
        Picasso.get().load(uri).centerCrop().resizeDimen(R.dimen.album_art, R.dimen.album_art).transform(ImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("lastFMArtistImage")
fun setLastFMArtistImage(view: ImageView, artist: String?) {
    if (artist != null && artist.isNotEmpty())
        DataHandler.lastfmRepository.getArtistInfo(artist).observeForever {
            when (it) {
                is Outcome.Success -> Picasso.get().load(it.data.artist.artwork[0].url).placeholder(R.drawable.ic_music_note).into(view)
            }
        }
}

@BindingAdapter("circleImageUrl")
fun setCircleImage(view: ImageView, uri: String) {
    if (uri.isNotEmpty())
        Picasso.get().load(uri).centerCrop().resizeDimen(R.dimen.album_art_circle_small, R.dimen.album_art_circle_small).transform(CircleTransform()).placeholder(R.drawable.ic_music_note).into(view)
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