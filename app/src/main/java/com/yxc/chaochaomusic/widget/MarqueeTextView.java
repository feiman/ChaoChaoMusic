package com.yxc.chaochaomusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 这个是为实现跑马灯效果自定义TextView，为什么要自定义一个，看下面：
 *  单个TextView直接设置属性即可实现，但当我们需要多个TextView实现跑马灯效果时，就不管用了，因为它获取不到焦点了。
 *  所以就需要自定义一个TextView，继承TextView，并且重写isFocuse()方法，让它永远返回true。
 */
public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
