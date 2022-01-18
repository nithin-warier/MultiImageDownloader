package com.androidians.multiimageuploader

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.androidians.multiimageuploader.Utils.IMAGE_CACHE_FILE_NAME
import com.androidians.multiimageuploader.Utils.IMAGE_MIME_TYPE
import com.androidians.multiimageuploader.Utils.URL_1
import com.androidians.multiimageuploader.Utils.URL_2
import com.androidians.multiimageuploader.Utils.URL_3
import com.androidians.multiimageuploader.Utils.URL_4
import com.androidians.multiimageuploader.databinding.ActivityMainBinding
import java.io.File

const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    // download multiple images from url and after downloading all images show in UI
    // this downloading has to happen on background, even when app is not in foreground

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val downloadManager: DownloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
    var downloadId: Long = 0
    var index = 0
    private val downloadedUriList = ArrayList<String>()
    private val urlList = ArrayList<String>()

    // once downloaded image from remote server complete, this receiver gets called
    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            queryDownloads()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        registerReceiver(onCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        addUrls()
        downloadImage(urlList[index])
    }

    private fun addUrls() {
        urlList.add(URL_1)
        urlList.add(URL_2)
        urlList.add(URL_3)
        urlList.add(URL_4)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(onCompleteReceiver)
    }

    @SuppressLint("Range")
    private fun queryDownloads() {
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        cursor.moveToFirst()
        val uriStr = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        Log.d(TAG, "download, uriStr: $uriStr")
        downloadedUriList.add(uriStr)

        if (index++ < urlList.size-1) {
            downloadImage(urlList[index])
        } else {
            binding.iv1.setImageURI(Uri.parse(downloadedUriList[--index]))
            binding.iv2.setImageURI(Uri.parse(downloadedUriList[--index]))
            binding.iv3.setImageURI(Uri.parse(downloadedUriList[--index]))
            binding.iv4.setImageURI(Uri.parse(downloadedUriList[--index]))
        }
    }

    private fun downloadImage(randomImageUrl: String) {
        Log.d(TAG, "downloadImage(), from remote server - randomImageUrl: $randomImageUrl")
        val file = File(getExternalFilesDir(null), IMAGE_CACHE_FILE_NAME)
        // we can make the image coming randomized using this URL - "https://picsum.photos/200/300"
        val downloadUri: Uri = Uri.parse(randomImageUrl)
        val request = DownloadManager.Request(downloadUri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(randomImageUrl)
            .setMimeType(IMAGE_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(file.toUri())
        downloadId = downloadManager.enqueue(request)
    }
}