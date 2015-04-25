/**
 * 
 */

package com.umeng.comm.ui.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 解决开发者使用ViewPager的方式并使用Fragment的方式集成，ViewPager嵌套ViewPager的滑动事件冲突
 */
public class CommunityViewPager extends ViewPager {

    private ViewPager mSubViewPager;

    /**
     * @param context
     */
    public CommunityViewPager(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public CommunityViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean arg1, int dx, int arg3, int arg4) {
        if (v != this && v instanceof ViewPager) {
            // 处于社区的viewpager，且在“所有”页面且向做滑动操作
            if (mSubViewPager != null && dx > 0 && (mSubViewPager.getCurrentItem() == 0)) {
                return false;
            } else if (mSubViewPager != null
                    && dx < 0
                    && (mSubViewPager.getCurrentItem() == mSubViewPager.getAdapter().getCount() - 1)) {
                // 处于社区的viewpager，且在“所有”页面且向做滑动操作
                return false;
            }
        }
        return super.canScroll(v, arg1, dx, arg3, arg4);
    }

}
