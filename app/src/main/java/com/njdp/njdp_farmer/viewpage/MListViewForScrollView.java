package com.njdp.njdp_farmer.viewpage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by qinlei on 2016/4/25.
 * 自定义滚动条
 */
public class MListViewForScrollView extends ListView {
    public MListViewForScrollView(Context context) {
        super(context);
    }

    public MListViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MListViewForScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
               MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
