package com.zeewain.base.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class CustomToast {

    private static Toast m_toast;
    private static Handler m_handler = new Handler();
    private static Runnable m_runnable = new Runnable()
    {
        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            m_toast.cancel();
        }
    };

    public static void showToast(Context context, String text, int duration)
    {
        m_handler.removeCallbacks(m_runnable);
        if( m_toast != null )
        {
            m_toast.setText(text);
        }
        else
        {
            m_toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        m_handler.postDelayed(m_runnable, duration);
        m_toast.show();
    }

    public static void showToast(Context context, int resId, int duration)
    {
        showToast(context, context.getResources().getString(resId), duration);
    }
};
