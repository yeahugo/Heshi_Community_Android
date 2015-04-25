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

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.text.TextUtils;

import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;

/**
 * 默认的地理位置提供者
 */
public class DefaultLocationProvider implements SocializeLocationProvider {
    private static final String TAG = DefaultLocationProvider.class.getName();
    private Location mLocation;
    private Context mContext;
    private SocializeLocationManager mLocationManager;
    private SocializeLocationListener mListener = null;
    private String mProvider;

    public DefaultLocationProvider() {
        super();
    }

    @Override
    public void init(Context context) {
        this.mContext = context;
        mListener = new SocializeLocationListener();
        getLocation();
    }
    
    /**
     * 
     * 移除获取地理位置信息的回调</br>
     */
    public void destroy() {
        if (mLocationManager != null && mListener != null) {
            mLocationManager.removeUpdates(mListener);
        }
    }

    @Override
    public Location getLocation() {

        if (mLocation == null) {
            if (DeviceUtils.checkPermission(mContext, "android.permission.ACCESS_FINE_LOCATION")) {
                requestLocation(mContext, Criteria.ACCURACY_FINE);
            } else if (DeviceUtils.checkPermission(mContext,
                    "android.permission.ACCESS_COARSE_LOCATION")) {
                requestLocation(mContext, Criteria.ACCURACY_COARSE);
            }
        }

        return mLocation;
    }

    /**
     * 
     * 获取地理位置信息</br>
     * @param context
     * @param accuracy
     */
    private void requestLocation(Context context, int accuracy) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(accuracy);

        String provider = mLocationManager.getBestProvider(criteria, true);
        if (provider != null) {
            mProvider = provider;
        }

        Log.d(TAG, "Get location from " + mProvider);

        try {
            if (!TextUtils.isEmpty(mProvider)) {
                Location mostRecentLocation = mLocationManager.getLastKnownLocation(mProvider);

                if (mostRecentLocation != null) {
                    mLocation = mostRecentLocation;
                } else if (mLocationManager.isProviderEnabled(mProvider) && mListener != null) {
                    if (context instanceof Activity) {
                        mLocationManager.requestLocationUpdates((Activity) context, mProvider, 1,
                                0,
                                mListener);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // "IllegalArgumentException 	if provider or listener is null"
        }
    }

    /**
     * 
     */
    @Override
    public void setLocationManager(SocializeLocationManager locationManager) {
        this.mLocationManager = locationManager;
    }

    protected SocializeLocationManager getLocationManager() {
        return mLocationManager;
    }

    protected void setLocation(Location location) {
        this.mLocation = location;
    }

    @Override
    public void setProvider(String provider) {
        this.mProvider = provider;
    }
}
