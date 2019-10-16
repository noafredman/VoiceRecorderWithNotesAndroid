package com.NoaoN.voiceRecorderWithNotes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.NoaoN.voiceRecorderWithNotes.R;
import com.NoaoN.voiceRecorderWithNotes.components.SliderAdapter;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private Button backBtn, nextBtn;
    private TextView[] dots;
    private final int NUM_OF_SLIDES = 4;
    private int currentPage = 0;
    private boolean finishedIntroAlready = false;
    private static boolean finishedClicked = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // I use SharedPreferences to know if user opened app for the first time.
        //check if user opened app for the first time.
        if (getSaveToPreference() != finishedIntroAlready) {
            startApp();
        } else {
            final int FIRST_PAGE = 0;
            setContentView(R.layout.activity_welcome);
            initializeComponents();
            SliderAdapter sliderAdapter = new SliderAdapter(this);
            viewPager.setAdapter(sliderAdapter);
            addDotsIndicator(FIRST_PAGE);
            viewPager.addOnPageChangeListener(viewListener);
            //set buttons click listeners
            setButtonsClickListeners();
        }
    }

    /**
     * Set buttons click listeners
     */
    private void setButtonsClickListeners() {
        backBtn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(currentPage - 1);
            }
        });
        nextBtn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (currentPage < NUM_OF_SLIDES - 1) {
                    viewPager.setCurrentItem(currentPage + 1);
                } else {
                    //on last page
                    setSaveToPreference();
                    if (finishedClicked) {
                        nextBtn.setClickable(false);
                        startApp();
                    }
                }

            }
        });
    }

    /**
     * Initialize activity's components.
     */
    private void initializeComponents() {
        backBtn = findViewById(R.id.backBtn);
        nextBtn = findViewById(R.id.nextBtn);
        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.dotsLayout);
    }

    /**
     * Add page dots indicator.
     *
     * @param pageNum - page number.
     */
    public void addDotsIndicator(int pageNum) {
        this.dots = new TextView[NUM_OF_SLIDES];
        for (int i = 0; i < NUM_OF_SLIDES; i++) {
            TextView tv = new TextView(this);
            tv.setText(Html.fromHtml("&#8226;"));
            tv.setTextSize(35);
            tv.setTextColor(getColor(R.color.greyBlueLight));
            if (i == pageNum) {
                tv.setTextColor(getColor(R.color.greyPurpleLight));
            }
            dots[i] = tv;
            dotsLayout.addView(tv);
        }
    }

    /**
     * Set color of the page dots indicator.
     *
     * @param pageNum - page number.
     */
    private void setDotsColor(int pageNum) {
        for (int i = 0; i < NUM_OF_SLIDES; i++) {
            dots[i].setTextColor(getColor(R.color.greyBlueLight));
            if (i == pageNum) {
                //currently viewed page
                dots[i].setTextColor(getColor(R.color.greyPurpleLight));
            }
        }
    }

    //set page changed listener.
    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            currentPage = i;
            if (currentPage < NUM_OF_SLIDES) {
                setDotsColor(currentPage);
                if (0 == currentPage) {
                    //on first page
                    backBtn.setVisibility(View.INVISIBLE);
                } else if (0 < currentPage && NUM_OF_SLIDES - 1 > currentPage) {
                    backBtn.setVisibility(View.VISIBLE);
                    nextBtn.setText(R.string.next_page);
                } else if (NUM_OF_SLIDES - 1 == currentPage) {
                    //on last page
                    backBtn.setVisibility(View.VISIBLE);
                    nextBtn.setText(R.string.finish_intro);
                }
            } else {
                setSaveToPreference();
                startApp();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * Saves preferences.
     */
    private void setSaveToPreference() {
        SharedPreferences sharedPref = getSharedPreferences("finished intro", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("finished intro", true);
        editor.apply();
    }

    /**
     * Returns preferences.
     *
     * @return preferences.
     */
    private boolean getSaveToPreference() {
        SharedPreferences sharedPref = getSharedPreferences("finished intro", MODE_PRIVATE);
        boolean pref = sharedPref.getBoolean("finished intro", finishedIntroAlready);
        return pref;
    }

    /**
     * Goes to recording (main) activity.
     */
    private void startApp() {
        Intent intent = new Intent(WelcomeActivity.this, RecordingActivity.class);
        startActivity(intent);
    }

}
