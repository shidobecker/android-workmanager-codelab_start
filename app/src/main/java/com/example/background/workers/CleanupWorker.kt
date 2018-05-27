package com.example.background.workers

import android.util.Log
import androidx.work.Worker
import com.example.background.Constants
import java.io.File

class CleanupWorker : Worker() {
    override fun doWork(): WorkerResult {
        try {

            val outputDirectory = File(applicationContext.filesDir, Constants.OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null && entries.isNotEmpty()) {
                    entries.forEach {
                        val name = it.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = it.delete()
                            Log.i("CleanupWorker", "Deleted ${name} is $deleted")
                        }
                    }
                }
            }

            return WorkerResult.SUCCESS

        } catch (e: Exception) {
            return WorkerResult.FAILURE
        }
    }
}