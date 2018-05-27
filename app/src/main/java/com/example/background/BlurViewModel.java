/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveImageToFileWorker;

import java.util.List;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.TAG_OUTPUT;

public class BlurViewModel extends ViewModel {

    private Uri mImageUri;

    private WorkManager workManager;

    // New instance variable for the WorkStatus
    private Uri mOutputUri;
    // Add a getter and setter for mOutputUri
    void setOutputUri(String outputImageUri) {
        mOutputUri = uriOrNull(outputImageUri);
    }
    Uri getOutputUri() { return mOutputUri; }

    public BlurViewModel() {
        workManager = WorkManager.getInstance();
        mSavedWorkStatus = workManager.getStatusesByTag(TAG_OUTPUT);
    }

    private LiveData<List<WorkStatus>> mSavedWorkStatus;

    LiveData<List<WorkStatus>> getOutputStatus() { return mSavedWorkStatus; }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     *
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {
        // Add WorkRequest to Cleanup temporary images
        //WorkContinuation workContinuation = workManager.beginWith(OneTimeWorkRequest.from(CleanupWorker.class));


       /*
        Sometimes you only want one chain of work to run at a time. For example,
         perhaps you have a work chain that syncs your local data with the server -
         you probably want to let the first data sync finish before starting a new one.
          To do this, you would use beginUniqueWork instead of beginWith; and you provide a unique
          String name. This names the entire chain of work requests so that you can refer to and
          query them together.

        Ensure that your chain of work to blur your file is unique by using beginUniqueWork.
        Pass in IMAGE_MANIPULATION_WORK_NAME as the key. You'll also need to pass in a ExisitingWorkPolicy.
        Your options are REPLACE, KEEP or APPEND.

        You'll use REPLACE because if the user decides to blur another image before the current one
        is finished, we want to stop the current one and start blurring the new image.

        The code for starting your unique work continuation is below:*/
        WorkContinuation workContinuation = workManager
                .beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker.class));

        // Add WorkRequests to blur the image the number of times requested
        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurRequest = new OneTimeWorkRequest.Builder(BlurWorker.class);
            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if (i == 0) {
                blurRequest.setInputData(createInputDataForUri());
            }

            workContinuation = workContinuation.then(blurRequest.build());
        }

        OneTimeWorkRequest saveRequest = new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                .addTag(TAG_OUTPUT)
                .build();
        workContinuation = workContinuation.then(saveRequest);

        //Actually start the work
        workContinuation.enqueue();


    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(Constants.KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

}