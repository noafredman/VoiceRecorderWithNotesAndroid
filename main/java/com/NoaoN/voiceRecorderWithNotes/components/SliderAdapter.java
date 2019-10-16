package com.NoaoN.voiceRecorderWithNotes.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.NoaoN.voiceRecorderWithNotes.R;

import org.jetbrains.annotations.NotNull;

/**
 * Slider Images for app walk-through, in WelcomeActivity.
 */
public class SliderAdapter extends PagerAdapter {
    private Context context;

    //Arrays
    private int [] images = {
            R.mipmap.rec_act_time_stamp_hd,
            R.mipmap.save_location_hd,
            R.mipmap.play_rec_act_note_hd,
            R.mipmap.rename_recording
    };

    /**
     * Constructor
     * @param context - current activity.
     */
    public SliderAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (ConstraintLayout) o;
    }


    /**
     * Create the page for the given position.
     * @param container - The containing View in which the page will be shown.
     * @param position  - The page position to be instantiated.
     * @return An Object representing the new page. This does not need
     *         to be a View, but can be some other container of the page.
     */
    @NotNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, null);
        ImageView image = view.findViewById(R.id.sliderImage);
        image.setImageResource(this.images[position]);
        container.addView(view);
        return view;
    }

    /**
     * Remove a page for the given position.
     * @param container - The containing View from which the page will be removed.
     * @param position - The page position to be removed.
     * @param view - The same object that was returned by instantiateItem(View, int).
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        (container).removeView((View) view);
    }

}
