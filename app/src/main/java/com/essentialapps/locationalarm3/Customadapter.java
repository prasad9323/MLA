package com.essentialapps.locationalarm3;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Customadapter extends PagerAdapter {
    private int[] images = {R.drawable.i1,R.drawable.i2,R.drawable.i3,R.drawable.i4,R.drawable.i5};
    private Context ctx;
    Integer number;
    private LayoutInflater layoutinflater;
    String[] fullsteps = {"Turn on data and location","Press 'Add alert' button","Select line","Select station","Press green button"};
    public Customadapter (Context ctx){
        this.ctx=ctx;
    }
    @Override
    public int getCount() {
        return images.length;
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(LinearLayout)object);
    }
    @Override
    public Object instantiateItem (ViewGroup container,int position){
        layoutinflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutinflater.inflate(R.layout.swipe_layout,container,false);
        ImageView img_view = (ImageView)item_view.findViewById(R.id.imageView);
        TextView text_view = (TextView)item_view.findViewById(R.id.textView);
        img_view.setImageResource(images[position]);
        number = position+1;
        text_view.setText("Step "+number);
        TextView full_steps = (TextView)item_view.findViewById(R.id.textView2);
        full_steps.setText(fullsteps[position]);
        container.addView(item_view);
        return item_view;
    }
    @Override
    public void destroyItem(ViewGroup container,int position,Object object) {
        container.removeView((LinearLayout)object);
    }
}
