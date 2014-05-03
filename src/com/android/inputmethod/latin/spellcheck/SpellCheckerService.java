/*
* Copyright (C) 2011 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package com.android.inputmethod.latin.spellcheck;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
* SpellCheckerService provides an abstract base class for a spell checker.
* This class combines a service to the system with the spell checker service interface that
* spell checker must implement.
*
* <p>In addition to the normal Service lifecycle methods, this class
* introduces a new specific callback that subclasses should override
* {@link #createSession()} to provide a spell checker session that is corresponding
* to requested language and so on. The spell checker session returned by this method
* should extend {@link SpellCheckerService.Session}.
* </p>
*
* <h3>Returning spell check results</h3>
*
* <p>{@link SpellCheckerService.Session#onGetSuggestions(TextInfo, int)}
* should return spell check results.
* It receives {@link android.view.textservice.TextInfo} and returns
* {@link android.view.textservice.SuggestionsInfo} for the input.
* You may want to override
* {@link SpellCheckerService.Session#onGetSuggestionsMultiple(TextInfo[], int, boolean)} for
* better performance and quality.
* </p>
*
* <p>Please note that {@link SpellCheckerService.Session#getLocale()} does not return a valid
* locale before {@link SpellCheckerService.Session#onCreate()} </p>
*
*/
public abstract class SpellCheckerService extends Service {
	private static final String TAG = SpellCheckerService.class.getSimpleName();
	private static final boolean DBG = false;
	public static final String SERVICE_INTERFACE =
		"android.service.textservice.SpellCheckerService";
	
	private final IBinder mBinder = null;
	
	
	/**
	 * Implement to return the implementation of the internal spell checker
	 * service interface. Subclasses should not override.
	 */
	@Override
	public final IBinder onBind(final Intent intent) {
	    if (DBG) {
		Log.w(TAG, "onBind");
	    }
	    return mBinder;
	}
	
	/**
	 * Factory method to create a spell checker session impl
	 * @return SpellCheckerSessionImpl which should be overridden by a concrete implementation.
	 */
	public abstract Session createSession();
	
	/**
	 * This abstract class should be overridden by a concrete implementation of a spell checker.
	 */
	public static abstract class Session {
	
	    /**
	     * This is called after the class is initialized, at which point it knows it can call
	     * getLocale() etc...
	     */
	    public abstract void onCreate();
	
	    /**
	     * Get suggestions for specified text in TextInfo.
	     * This function will run on the incoming IPC thread.
	     * So, this is not called on the main thread,
	     * but will be called in series on another thread.
	     * @param textInfo the text metadata
	     * @param suggestionsLimit the number of limit of suggestions returned
	     * @return SuggestionsInfo which contains suggestions for textInfo
	     */
	    public abstract SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit);
	
	    /**
	     * A batch process of onGetSuggestions.
	     * This function will run on the incoming IPC thread.
	     * So, this is not called on the main thread,
	     * but will be called in series on another thread.
	     * @param textInfos an array of the text metadata
	     * @param suggestionsLimit the number of limit of suggestions returned
	     * @param sequentialWords true if textInfos can be treated as sequential words.
	     * @return an array of SuggestionsInfo of onGetSuggestions
	     */
	    public SuggestionsInfo[] onGetSuggestionsMultiple(TextInfo[] textInfos,
		    int suggestionsLimit, boolean sequentialWords) {
			final int length = textInfos.length;
			final SuggestionsInfo[] retval = new SuggestionsInfo[length];
			for (int i = 0; i < length; ++i) {
			    retval[i] = onGetSuggestions(textInfos[i], suggestionsLimit);
			}
			return retval;
	    }
	
	    /**
	     * Request to abort all tasks executed in SpellChecker.
	     * This function will run on the incoming IPC thread.
	     * So, this is not called on the main thread,
	     * but will be called in series on another thread.
	     */
	    public void onCancel() {}
	
	    /**
	     * Request to close this session.
	     * This function will run on the incoming IPC thread.
	     * So, this is not called on the main thread,
	     * but will be called in series on another thread.
	     */
	    public void onClose() {}
	
	    /**
	     * @return Locale for this session
	     */
	    public String getLocale() {
	    	return "en";
	    }
	
	    /**
	     * @return Bundle for this session
	     */
	    public Bundle getBundle() {
	    	return null;
	    }
	}

}