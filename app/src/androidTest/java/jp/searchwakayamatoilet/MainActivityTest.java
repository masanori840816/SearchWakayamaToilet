/**
 * Created by masanori on 2016/02/13.
 * this is test class for MainActivity.java.
 */

package jp.searchwakayamatoilet;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule(MainActivity.class);

    @Before
    public void setUp() throws Exception {

    }
    @Test
    public void hasToolbar() throws Exception{
        onView(ViewMatchers.withId(R.id.toolbar)).check(matches(isDisplayed()));
    }
    @Test
    public void hasSearchView() throws Exception{
        onView(ViewMatchers.withId(R.id.searchview)).check(matches(isDisplayed()));
    }
    @Test
    public void hasEditTextOnSearchView() throws Exception{
        onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click());
        onView(ViewMatchers.isAssignableFrom(EditText.class)).perform(ViewActions.typeText("test"), ViewActions.pressKey(66));
    }
    @Test
    public void isSuggestSearchAllShown() throws Exception{
        onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click());
        onView(ViewMatchers.isAssignableFrom(EditText.class)).perform(ViewActions.typeText(" "), ViewActions.replaceText(""));
        onView(ViewMatchers.withId(R.id.suggest_list)).check(matches(isDisplayed()));
    }
    @Test
    public void isMethodSetMarkersByFreeWordCalled() throws Exception{
        hasEditTextOnSearchView();

    }
}
