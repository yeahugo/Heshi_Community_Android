/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.umeng.comm.ui.location;

import android.content.Context;
import android.location.Location;

import com.umeng.comm.core.listeners.Listeners.FetchListener;

/**
 * @author mrsimple
 */
public class LocationFinder {

    /**
     * 
     */
    private SocializeLocationProvider mProvider = new DefaultLocationProvider();
    /**
     * 
     */
    SocializeLocationManager locationManager = new SocializeLocationManager();

    private static LocationFinder mFinder = new LocationFinder();

    private LocationFinder() {

    }

    /**
     * @return
     */
    public static LocationFinder getInstance() {
        return mFinder;
    }

    /**
     * @param provider
     */
    public void setLocationProvider(SocializeLocationProvider provider) {
        mProvider = provider;

    }

    /**
     * @param context
     * @param listener
     */
    public void findLocation(final Context context, final FetchListener<Location> listener) {
        locationManager.init(context);
        mProvider.setLocationManager(locationManager);
        mProvider.init(context);

        new GetLocationTask(mProvider) {
            protected void onPostExecute(android.location.Location result) {
                if (listener != null) {
                    listener.onComplete(result);
                }
            };
        }.execute();
    }

}
