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
import android.location.LocationListener;
import android.location.LocationManager;
import android.text.TextUtils;

import com.umeng.comm.core.utils.DeviceUtils;

/**
 * <p>
 * @ClassName: SocializeLocationManager
 * </p>
 * <p>
 * @Description: 位置信息管理器, 通过本机获取经纬度,将经纬度传递给服务器,由服务器来获取逆地址解析、并且获取周边10个POI位置信息.
 * </p>
 */
public class SocializeLocationManager {
    LocationManager mLocationManager = null;

    public SocializeLocationManager() {
        super();
    }

    public void init(Context context) {
        if (DeviceUtils.checkPermission(context, "android.permission.ACCESS_FINE_LOCATION")
                || DeviceUtils.checkPermission(context,
                        "android.permission.ACCESS_COARSE_LOCATION")) {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        return (mLocationManager == null) ? null : mLocationManager.getBestProvider(criteria,
                enabledOnly);
    }

    public Location getLastKnownLocation(String provider) {
        return (mLocationManager == null) ? null : mLocationManager.getLastKnownLocation(provider);
    }

    public boolean isProviderEnabled(String provider) {
        return (mLocationManager == null) ? false : mLocationManager.isProviderEnabled(provider);
    }

    public void requestLocationUpdates(Activity activity, final String provider,
            final long minTime, final float minDistance, final LocationListener listener) {
        if (mLocationManager != null && !TextUtils.isEmpty(provider)) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLocationManager.requestLocationUpdates(provider, minTime, minDistance,
                            listener);
                }
            });
        }
    }

    public void removeUpdates(final LocationListener listener) {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(listener);
        }
    }
}
