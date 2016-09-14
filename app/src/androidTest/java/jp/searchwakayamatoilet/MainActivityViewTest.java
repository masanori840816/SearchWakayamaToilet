package jp.searchwakayamatoilet;

/**
 * Created by masanori on 2016/09/09.
 * this is view test class for MainActivity.java.
 */
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.support.test.espresso.matcher.BoundedMatcher;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import org.hamcrest.Matcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class MainActivityViewTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception{
    }

    @Test
    public void hasToolbar() throws Exception{
        Espresso.onView(ViewMatchers.withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    @Test
    public void hasSearchView() throws Exception{
        Espresso.onView(ViewMatchers.withId(R.id.searchview))
                .check(matches(isDisplayed()));
    }

    @Test
    public void hasEditTextOnSearchView() throws Exception{
        Espresso.onView(ViewMatchers.withId(R.id.searchview))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.isAssignableFrom(EditText.class))
                .perform(ViewActions.typeText("test"), ViewActions.pressKey(66));
    }

    @Test
    public void isSuggestSearchAllShown() throws Exception{
        Espresso.onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.isAssignableFrom(EditText.class))
            .perform(ViewActions.typeText(" "), ViewActions.replaceText(""));
        Espresso.onView(ViewMatchers.withId(R.id.suggest_list)).check(matches(isDisplayed()));
    }

    @Test
    public void hasAboutAppButtonInMenu() throws Exception{
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        Espresso.onView(ViewMatchers.withText(R.string.action_about))
                .perform(ViewActions.click());
    }

    @Test
    public void isAboutFragmentShown() throws Exception{
        hasAboutAppButtonInMenu();
        Espresso.onView(ViewMatchers.withId(R.id.aboutapp_activity)).check(matches(isDisplayed()));
    }
    @Test
    public void hasScrollViewOnAboutAppActivity() throws Exception{
        this.hasAboutAppButtonInMenu();
        Espresso.onView(ViewMatchers.withId(R.id.about_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void hasAboutProjectViewOnAboutAppActivity() throws Exception{
        hasAboutAppButtonInMenu();
        Context context = InstrumentationRegistry.getTargetContext();

        Espresso.onData(getAboutAppListAreaTitle(context.getString(R.string.about_title))).check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_description))).check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_project_url))).check(matches(isDisplayed()));
    }
    @Test
    public void hasCreditsToiletmapOnAboutAppActivity() throws Exception{
        hasAboutAppButtonInMenu();

        Context context = InstrumentationRegistry.getTargetContext();
        Espresso.onData(getAboutAppListAreaTitle(context.getString(R.string.about_credits_title))).check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_toiletmap))).check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_toiletmap))).check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_toiletmap_url))).check(matches(isDisplayed()));
    }
    @Test
    public void hasCreaditsRxJavaOnAboutAppActivity() throws Exception{
        hasAboutAppButtonInMenu();

        Context context = InstrumentationRegistry.getTargetContext();
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_rxjava)))
                .check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_rxjava)))
                .check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_rxjava_url)))
                .check(matches(isDisplayed()));
    }
    @Test
    public void hasCreaditsRxAndroidOnAboutAppActivity() throws Exception{
        hasAboutAppButtonInMenu();

        Context context = InstrumentationRegistry.getTargetContext();
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_rxandroid)))
                .check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_rxandroid)))
                .check(matches(isDisplayed()));
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_rxandroid_url)))
                .check(matches(isDisplayed()));
    }
    private Matcher<Object> getAboutAppListAreaTitle(String itemTitle) {
        return new BoundedMatcher<Object, AboutAppListItem>(AboutAppListItem.class) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("AboutAppListItem with AreaTitle: " + itemTitle);
            }
            @Override
            public boolean matchesSafely(AboutAppListItem aboutAppDataItem){
                return itemTitle.equals(aboutAppDataItem.getAreaTitle());
            }
        };
    }
    private Matcher<Object> getAboutAppListItemTitle(String itemTitle){
        return new BoundedMatcher<Object, AboutAppListItem>(AboutAppListItem.class) {
            @Override
            public void  describeTo(org.hamcrest.Description description) {
                description.appendText("AboutAppListItem with ItemTitle: " + itemTitle);
            }
            @Override
            public boolean matchesSafely(AboutAppListItem aboutAppDataItem){
                return itemTitle.equals(aboutAppDataItem.getItemTitle());
            }
        };
    }
    private Matcher<Object> getAboutAppListDescription(String itemTitle) {
        return new BoundedMatcher<Object, AboutAppListItem>(AboutAppListItem.class) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("AboutAppListItem with ItemTitle: " + itemTitle);
            }
            @Override
            public boolean matchesSafely(AboutAppListItem aboutAppDataItem){
                return itemTitle.equals(aboutAppDataItem.getDescription());
            }
        };
    }
    private Matcher<Object> getAboutAppListLink(String itemTitle){
        return new BoundedMatcher<Object, AboutAppListItem>(AboutAppListItem.class) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("AboutAppListItem with ItemTitle: " + itemTitle);
            }
            @Override
            public boolean matchesSafely(AboutAppListItem aboutAppDataItem){
                return itemTitle.equals(aboutAppDataItem.getLink());
            }
        };
    }
}