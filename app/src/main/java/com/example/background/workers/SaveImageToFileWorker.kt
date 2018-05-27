package com.example.background.workers

import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.Worker
import com.example.background.Constants
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker : Worker() {

    private val TAG = SaveImageToFileWorker::class.java.simpleName

    private val TITLE = "Blurred Image"
    private val DATE_FORMATTER = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    override fun doWork(): WorkerResult {
        val resolver = applicationContext.contentResolver
        WorkerUtils.makeStatusNotification("Saving image", applicationContext)
        WorkerUtils.sleep()
        try {
            val resourceUri = inputData.getString(Constants.KEY_IMAGE_URI, null)

            val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))
            val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, TITLE, DATE_FORMATTER.format(Date()))

            if (imageUrl.isEmpty()) {
                Log.e(TAG, "Writing to MediaStore failed")
                return WorkerResult.FAILURE
            }
            //Setting the data output after save the image
            val output = Data.Builder().putString(Constants.KEY_IMAGE_URI, imageUrl).build()
            outputData = output
            return WorkerResult.SUCCESS
        } catch (ex: Exception) {
            Log.e("SaveImage", "Unable To Save Image to Gallery")
            return WorkerResult.FAILURE
        }

    }

}