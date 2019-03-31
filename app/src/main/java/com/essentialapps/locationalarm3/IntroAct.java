package com.essentialapps.locationalarm3;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class IntroAct extends AppCompatActivity {
    ViewPager viewPager;
    View view;
    private GestureDetectorCompat gestureDetectorCompat;
    Customadapter customadapter;
    Integer n, temp, item1;
    Button b1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        temp = 0;
        b1 = (Button) findViewById(R.id.button1);
        final Button b2 = (Button) findViewById(R.id.button2);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(4);
        customadapter = new Customadapter(getApplicationContext());
        viewPager.setAdapter(customadapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        String s = Integer.toString(temp);
        b1.setTextColor(Color.parseColor("#ffffff"));
        b1.setText("Next");
        b2.setEnabled(false);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                item1 = viewPager.getCurrentItem();
                if (item1.equals(4)) {
                    b1.setText("");
                    b2.setTextColor(Color.parseColor("#ffffff"));
                    b2.setEnabled(true);
                    b2.setText("Done");
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Launcher.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });
        //setStatusBarColor(findViewById(R.id.statusBarBackground), getResources().getColor(android.R.color.transparent));
        // view = this.getWindow().getDecorView();
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
               /* temp = viewPager.getCurrentItem();
                String s = Integer.toString(temp);
                //  Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                if(temp==4) {
                    b1.setText("Done!");
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.col5));
                    b1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(),Launcher.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            overridePendingTransition(R.anim.animation3, R.anim.animation4);
                            finish();
                        }
                    });
                }
                else
                {
                    b1.setText("Next");
                    b1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                        }
                    });
                }
                if(temp==3)
                {
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.col4));
                    overridePendingTransition(R.anim.animation3, R.anim.animation4);
                }
                if(temp==2)
                {
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.col3));
                    overridePendingTransition(R.anim.animation3, R.anim.animation4);
                }
                if(temp==1)
                {
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.col2));
                    overridePendingTransition(R.anim.animation3, R.anim.animation4);
                }
                if(temp==0)
                {
                    getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.col1));
                    overridePendingTransition(R.anim.animation3, R.anim.animation4);
                }*/
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Your code here
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public void setStatusBarColor(View statusBar, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int actionBarHeight = getActionBarHeight();
            int statusBarHeight = getStatusBarHeight();
            statusBar.getLayoutParams().height = actionBarHeight + statusBarHeight;
            statusBar.setBackgroundColor(color);
        }
    }

    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
