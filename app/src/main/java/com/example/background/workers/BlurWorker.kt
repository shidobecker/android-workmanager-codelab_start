package com.example.background.workers

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import com.example.background.Constants


class BlurWorker : Worker() {

    override fun doWork(): WorkerResult {
        try {
            //Input Data is a parameter que can be passed to doWork
            val resourceUri = inputData.getString(Constants.KEY_IMAGE_URI, null)

            if(resourceUri.isEmpty()){
                Log.e("Error", "Invalid URI")
                throw IllegalArgumentException("Invalid URI")
            }

            val resolver = applicationContext.contentResolver

            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

            //Blur the bitmap
            val output = WorkerUtils.blurBitmap(picture, applicationContext)
            //Writes bitmap to file
            val outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output)

            outputData = Data.Builder().putString(Constants.KEY_IMAGE_URI, outputUri.toString()).build()
            WorkerUtils.makeStatusNotification("Output is ${outputUri}", applicationContext)
            return WorkerResult.SUCCESS
        } catch (exception: Exception) {
            // Technically WorkManager will return WorkerResult.FAILURE
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            return WorkerResult.FAILURE
        }

    }


}