package com.essentialapps.locationalarm3;


        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.provider.Settings;
        import android.util.TypedValue;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.view.animation.Animation;
        import android.view.animation.AnimationUtils;
        import android.widget.Button;
        import android.widget.Toast;

        import com.essentialapps.locationalarm3.R;

public class SplashAct extends Activity {
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //overridePendingTransition(R.anim.animation3, R.anim.animation4);
        //setStatusBarColor(findViewById(R.id.statusBarBackground), getResources().getColor(android.R.color.transparent));
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        final SharedPreferences.Editor editor = pref.edit();
        String s = pref.getString("First time", "yes");
        if(s.equals("yes")) {
            Thread timer2 = new Thread() {
                public void run() {
                    try {
                        sleep(4000);
                        Intent i = new Intent(SplashAct.this, Agree.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.animation3, R.anim.animation4);
                        finish();
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            timer2.start();
        }
        if(s.equals("no")) {
            Thread startTimer = new Thread() {
                public void run() {
                    try {
                        sleep(2000);
                        Intent i = new Intent(SplashAct.this, Launcher.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.animation3, R.anim.animation4);
                        finish();
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            startTimer.start();
        }
    }
    public void setStatusBarColor(View statusBar,int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int actionBarHeight = getActionBarHeight();
            int statusBarHeight = getStatusBarHeight();
            statusBar.getLayoutParams().height = actionBarHeight + statusBarHeight;
            statusBar.setBackgroundColor(color);
        }
    }
    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}

