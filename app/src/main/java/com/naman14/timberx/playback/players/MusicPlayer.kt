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
package com.naman14.timberx.playback.players

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import timber.log.Timber

typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit

/**
 * An injectable wrapper around [MediaPlayer].
 *
 * @author Aidan Follestad (@afollestad)
 */
interface MusicPlayer {
    fun play()

    fun setSource(path: String): Boolean

    fun setSource(uri: Uri): Boolean

    fun prepare()

    fun seekTo(position: Int)

    fun isPrepared(): Boolean

    fun isPlaying(): Boolean

    fun position(): Int

    fun pause()

    fun stop()

    fun reset()

    fun release()

    fun onPrepared(prepared: OnPrepared<MusicPlayer>)

    fun onError(error: OnError<MusicPlayer>)

    fun onCompletion(completion: OnCompletion<MusicPlayer>)
}

class RealMusicPlayer(internal val context: Application) : MusicPlayer,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private var _player: MediaPlayer? = null
    private val player: MediaPlayer
        get() {
            if (_player == null) {
                _player = createPlayer(this)
            }
            return _player ?: throw IllegalStateException("Impossible")
        }

    private var didPrepare = false
    private var onPrepared: OnPrepared<MusicPlayer> = {}
    private var onError: OnError<MusicPlayer> = {}
    private var onCompletion: OnCompletion<MusicPlayer> = {}

    override fun play() {
        Timber.d("play()")
        player.start()
    }

    override fun setSource(path: String): Boolean {
        Timber.d("setSource() - $path")
        try {
            player.setDataSource(path)
        } catch (e: Exception) {
            Timber.d("setSource() - failed")
            onError(this, e)
            return false
        }
        return true
    }

    override fun setSource(uri: Uri): Boolean {
        Timber.d("setSource() - $uri")
        try {
            player.setDataSource(context, uri)
        } catch (e: Exception) {
            Timber.d("setSource() - failed")
            onError(this, e)
            return false
        }
        return true
    }

    override fun prepare() {
        Timber.d("prepare()")
        player.prepareAsync()
    }

    override fun seekTo(position: Int) {
        Timber.d("seekTo($position)")
        player.seekTo(position)
    }

    override fun isPrepared() = didPrepare

    override fun isPlaying() = player.isPlaying

    override fun position() = player.currentPosition

    override fun pause() {
        Timber.d("pause()")
        player.pause()
    }

    override fun stop() {
        Timber.d("stop()")
        player.stop()
    }

    override fun reset() {
        Timber.d("reset()")
        player.reset()
    }

    override fun release() {
        Timber.d("release()")
        player.release()
        _player = null
    }

    override fun onPrepared(prepared: OnPrepared<MusicPlayer>) {
        this.onPrepared = prepared
    }

    override fun onError(error: OnError<MusicPlayer>) {
        this.onError = error
    }

    override fun onCompletion(completion: OnCompletion<MusicPlayer>) {
        this.onCompletion = completion
    }

    // Callbacks from stock MediaPlayer...

    override fun onPrepared(mp: MediaPlayer?) {
        Timber.d("onPrepared()")
        didPrepare = true
        onPrepared(this)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        didPrepare = false
        Timber.d("onError() - what = $what, extra = $extra")
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Timber.d("onCompletion()")
        onCompletion(this)
    }
}

private fun createPlayer(owner: RealMusicPlayer): MediaPlayer {
    return MediaPlayer().apply {
        setWakeMode(owner.context, PowerManager.PARTIAL_WAKE_LOCK)
        val attr = AudioAttributes.Builder().apply {
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            setUsage(AudioAttributes.USAGE_MEDIA)
        }.build()
        setAudioAttributes(attr)
        setOnPreparedListener(owner)
        setOnCompletionListener(owner)
        setOnErrorListener(owner)
    }
}
