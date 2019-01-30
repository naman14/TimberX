package com.naman14.timberx.util

import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.naman14.timberx.R
import android.util.DisplayMetrics
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

object Utils {

    val MUSIC_ONLY_SELECTION = (MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''")

    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }

    fun makeShortTimeString(context: Context, _secs: Long): String {
        var secs = _secs
        val hours: Long
        val mins: Long

        hours = secs / 3600
        secs %= 3600
        mins = secs / 60
        secs %= 60

        val durationFormat = context.getResources().getString(
                if (hours == 0L) R.string.durationformatshort else R.string.durationformatlong)
        return String.format(durationFormat, hours, mins, secs)
    }

    fun isOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }


    enum class IdType private constructor(val mId: Int) {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);


        companion object {

            fun getTypeById(id: Int): IdType {
                for (type in values()) {
                    if (type.mId == id) {
                        return type
                    }
                }

                throw IllegalArgumentException("Unrecognized id: $id")
            }
        }
    }

    fun getEmptyAlbumArtUri(): String {
        return "android.resource://" + "com.naman14.timberx/drawable/icon"
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap
                .height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()

        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun makeLabel(context: Context, pluralInt: Int,
                  number: Int): String {
        return context.resources.getQuantityString(pluralInt, number, number)
    }

    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
        }

        return ""
    }

}