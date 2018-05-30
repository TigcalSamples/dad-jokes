package com.tigcal.dadjokes;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void getJoke() {
        onView(withId(R.id.joke_button))
                .perform(click());

        //TODO fix code: wait for a bit after API call
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.joke_text_view))
                .check(matches(not(withText(R.string.joke_tbd))));
    }

    @Test
    public void searchJoke() {
        onView(withId(R.id.action_search))
                .perform(click());

        onView(withId(android.support.design.R.id.search_src_text))
                .perform(typeText("dad\n"));

        onView(withId(R.id.joke_button))
                .perform(click());

        //TODO fix code: wait for a bit after API call
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.joke_text_view))
                .check(matches(withText(containsString("dad"))));
    }

}
