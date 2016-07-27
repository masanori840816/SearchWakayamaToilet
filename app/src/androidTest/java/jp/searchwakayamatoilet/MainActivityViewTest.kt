/**
 * Created by masanori on 2016/02/13.
 * this is view test class for MainActivity.java.
 */

package jp.searchwakayamatoilet

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.widget.EditText

import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.PreferenceMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matcher

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.spy

@RunWith(AndroidJUnit4::class)
class MainActivityViewTest {

    @Rule @JvmField
    val mainActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun setUp() {
    }

    @Test
    @Throws(Exception::class)
    fun hasToolbar() {
        Espresso.onView(ViewMatchers.withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasSearchView() {
        Espresso.onView(ViewMatchers.withId(R.id.searchview)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasEditTextOnSearchView() {
        Espresso.onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.isAssignableFrom(EditText::class.java)).perform(ViewActions.typeText("test"), ViewActions.pressKey(66))
    }

    @Test
    @Throws(Exception::class)
    fun isSuggestSearchAllShown() {
        Espresso.onView(ViewMatchers.withId(R.id.searchview)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.isAssignableFrom(EditText::class.java)).perform(ViewActions.typeText(" "), ViewActions.replaceText(""))
        Espresso.onView(ViewMatchers.withId(R.id.suggest_list)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasAboutAppButtonInMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())
        Espresso.onView(ViewMatchers.withText(R.string.action_about)).perform(ViewActions.click())
    }

    @Test
    @Throws(Exception::class)
    fun isAboutFragmentShown() {
        hasAboutAppButtonInMenu()
        Espresso.onView(ViewMatchers.withId(R.id.aboutapp_activity)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasScrollViewOnAboutAppActivity() {
        this.hasAboutAppButtonInMenu()
        Espresso.onView(ViewMatchers.withId(R.id.about_listview)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun hasAboutProjectViewOnAboutAppActivity() {
        hasAboutAppButtonInMenu()
        val context = InstrumentationRegistry.getTargetContext()

        Espresso.onData(getAboutAppListAreaTitle(context.getString(R.string.about_title))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_description))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_project_url))).check(matches(isDisplayed()))
    }
    @Test
    @Throws(Exception::class)
    fun hasCreditsToiletmapOnAboutAppActivity() {
        hasAboutAppButtonInMenu()

        val context = InstrumentationRegistry.getTargetContext()
        Espresso.onData(getAboutAppListAreaTitle(context.getString(R.string.about_credits_title))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_toiletmap))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_toiletmap))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_toiletmap_url))).check(matches(isDisplayed()))
    }
    @Test
    @Throws(Exception::class)
    fun hasCreaditsKotlinOnAboutAppActivity() {
        hasAboutAppButtonInMenu()

        val context = InstrumentationRegistry.getTargetContext()
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_kotlin))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_kotlin))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_kotlin_url))).check(matches(isDisplayed()))
    }
    @Test
    @Throws(Exception::class)
    fun hasCreaditsRxJavaOnAboutAppActivity() {
        hasAboutAppButtonInMenu()

        val context = InstrumentationRegistry.getTargetContext()
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_rxjava))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_rxjava))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_rxjava_url))).check(matches(isDisplayed()))
    }
    @Test
    @Throws(Exception::class)
    fun hasCreaditsRxAndroidOnAboutAppActivity() {
        hasAboutAppButtonInMenu()

        val context = InstrumentationRegistry.getTargetContext()
        Espresso.onData(getAboutAppListItemTitle(context.getString(R.string.about_credits_title_rxandroid))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListDescription(context.getString(R.string.about_credits_rxandroid))).check(matches(isDisplayed()))
        Espresso.onData(getAboutAppListLink(context.getString(R.string.about_credits_rxandroid_url))).check(matches(isDisplayed()))
    }
    private fun getAboutAppListAreaTitle(itemTitle: String): Matcher<Any> {
        return object: BoundedMatcher<Any, AboutAppListItem>(AboutAppListItem::class.java) {
                override fun describeTo(description: org.hamcrest.Description) {
                    description.appendText("AboutAppListItem with AreaTitle: " + itemTitle)
                }
                override fun matchesSafely(aboutAppDataItem: AboutAppListItem): Boolean {
                    return itemTitle.equals(aboutAppDataItem.getAreaTitle())
                }
            }
    }
    private fun getAboutAppListItemTitle(itemTitle: String): Matcher<Any> {
        return object: BoundedMatcher<Any, AboutAppListItem>(AboutAppListItem::class.java) {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("AboutAppListItem with ItemTitle: " + itemTitle)
            }
            override fun matchesSafely(aboutAppDataItem: AboutAppListItem): Boolean {
                return itemTitle.equals(aboutAppDataItem.getItemTitle())
            }
        }
    }
    private fun getAboutAppListDescription(itemTitle: String): Matcher<Any> {
        return object: BoundedMatcher<Any, AboutAppListItem>(AboutAppListItem::class.java) {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("AboutAppListItem with ItemTitle: " + itemTitle)
            }
            override fun matchesSafely(aboutAppDataItem: AboutAppListItem): Boolean {
                return itemTitle.equals(aboutAppDataItem.getDescription())
            }
        }
    }
    private fun getAboutAppListLink(itemTitle: String): Matcher<Any> {
        return object: BoundedMatcher<Any, AboutAppListItem>(AboutAppListItem::class.java) {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("AboutAppListItem with ItemTitle: " + itemTitle)
            }
            override fun matchesSafely(aboutAppDataItem: AboutAppListItem): Boolean {
                return itemTitle.equals(aboutAppDataItem.getLink())
            }
        }
    }
}
