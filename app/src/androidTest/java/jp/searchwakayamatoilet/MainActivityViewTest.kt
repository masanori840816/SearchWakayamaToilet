/**
 * Created by masanori on 2016/02/13.
 * this is view test class for MainActivity.java.
 */

package jp.searchwakayamatoilet

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.test.suitebuilder.annotation.LargeTest
import android.widget.EditText

import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.spy

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityViewTest {

    @Rule
    var mainActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun setUp() {
    }

    @Test
    @Throws(Exception::class)
    fun hasToolbar() {
        onView(ViewMatchers.withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasSearchView() {
        onView(ViewMatchers.withId(R.id.searchview)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasEditTextOnSearchView() {
        onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click())
        onView(ViewMatchers.isAssignableFrom(EditText::class.java)).perform(ViewActions.typeText("test"), ViewActions.pressKey(66))
    }

    @Test
    @Throws(Exception::class)
    fun isSuggestSearchAllShown() {
        onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click())
        onView(ViewMatchers.isAssignableFrom(EditText::class.java)).perform(ViewActions.typeText(" "), ViewActions.replaceText(""))
        onView(ViewMatchers.withId(R.id.suggest_list)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasAboutAppButtonInMenu() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
        onView(ViewMatchers.withText(R.string.action_about)).perform(ViewActions.click())
    }

    @Test
    @Throws(Exception::class)
    fun isAboutFragmentShown() {
        this.hasAboutAppButtonInMenu()
        onView(ViewMatchers.withId(R.id.about_fragment)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasScrollViewOnAboutAppFragment() {
        this.hasAboutAppButtonInMenu()
        onView(ViewMatchers.withId(R.id.about_scroll_view)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasAboutProjectViewOnAboutAppFragment() {
        this.hasAboutAppButtonInMenu()
        onView(ViewMatchers.withId(R.id.about_title)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_description)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_project_link)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasCreditsToiletmapViewOnAboutAppFragment() {
        this.hasAboutAppButtonInMenu()
        onView(ViewMatchers.withId(R.id.about_credits_title_toiletmap)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_credits_toiletmap)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_credits_toiletmap_link)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasCreaditsRetrolambdaViewOnAboutAppFragment() {
        this.hasAboutAppButtonInMenu()
        onView(ViewMatchers.withText(R.string.about_credits_retrolambda)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.about_credits_title_retrolambda)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_credits_retrolambda)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_credits_retrolambda_link)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasCreaditsLightweightStreamApiOnAboutAppFragment() {
        this.hasAboutAppButtonInMenu()
        onView(ViewMatchers.withText(R.string.about_credits_retrolambda)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withId(R.id.about_credits_title_lightweight_stream_api)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_credits_lightweight_stream_api)).check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.about_credits_lightweight_stream_api_link)).check(matches(isDisplayed()))
    }
}
