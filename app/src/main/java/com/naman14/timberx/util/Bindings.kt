package com.naman14.timberx.util

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import com.naman14.timberx.R
import com.naman14.timberx.network.Outcome
import com.naman14.timberx.network.api.LastFmDataHandler
import com.naman14.timberx.network.models.ArtistInfo
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
    fetchArtistImage(artist, 2, view)
}

@BindingAdapter("lastFMLargeArtistImage")
fun setLastFMLargeArtistImage(view: ImageView, artist: String?) {
    fetchArtistImage(artist, 4, view)
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

@BindingAdapter("repeatMode")
fun setRepeatMode(view: ImageView, mode: Int) {
    when (mode) {
        PlaybackStateCompat.REPEAT_MODE_NONE -> view.setImageResource(R.drawable.ic_repeat_none)
        PlaybackStateCompat.REPEAT_MODE_ONE -> view.setImageResource(R.drawable.ic_repeat_one)
        PlaybackStateCompat.REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat_all)
        else -> view.setImageResource(R.drawable.ic_repeat_none)

    }
}

@BindingAdapter("shuffleMode")
fun setShuffleMode(view: ImageView, mode: Int) {
    when (mode) {
        PlaybackStateCompat.SHUFFLE_MODE_NONE -> view.setImageResource(R.drawable.ic_shuffle_none)
        PlaybackStateCompat.SHUFFLE_MODE_ALL -> view.setImageResource(R.drawable.ic_shuffle_all)
        else -> view.setImageResource(R.drawable.ic_shuffle_none)
    }
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
}

private fun fetchArtistImage(artist: String?,  imageSizeIndex: Int, imageView: ImageView) {
    if (artist != null && artist.isNotEmpty()) {
        val artistData = LastFmDataHandler.lastfmRepository.getArtistInfo(artist)
        val observer: Observer<Outcome<ArtistInfo>> = object : Observer<Outcome<ArtistInfo>> {
            override fun onChanged(it: Outcome<ArtistInfo>?) {
                artistData.removeObserver(this)
                when (it) {
                    is Outcome.Success -> {
                        if (it.data.artist == null) return
                        val url = it.data.artist!!.artwork[imageSizeIndex].url
                        if (url.isNotEmpty())
                            Picasso.get().load(url).centerCrop().resizeDimen(R.dimen.album_art_large, R.dimen.album_art_large).transform(LargeImageTransformation.transformation(imageView.context)).placeholder(R.drawable.ic_music_note).into(imageView)
                    }
                }
            }
        }
        artistData.observeForever(observer)
    }
}