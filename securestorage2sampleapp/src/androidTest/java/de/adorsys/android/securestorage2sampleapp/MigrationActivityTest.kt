package de.adorsys.android.securestorage2sampleapp

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MigrationActivityTest {

    @Rule
    @JvmField
    var mainActivityTestRule = ActivityTestRule(MainActivity::class.java)

    private val TEST_VALUE = "TEST"

    @Test
    fun oldSolutionToNewSolutionMigrationTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(250)

        val overflowMenuButton = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                0),
                        isDisplayed()))
        overflowMenuButton.perform(click())

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(250)

        val migrateMenuButton = onView(
                allOf(withId(R.id.title), withText("Migrate"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()))
        migrateMenuButton.perform(click())

        val plainMessageEditText = onView(
                allOf(withId(R.id.plain_message_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")),
                                        0),
                                0)))
        plainMessageEditText.perform(ViewActions.scrollTo(), ViewActions.replaceText(TEST_VALUE), ViewActions.closeSoftKeyboard())

        val oldSolutionEncryptButton = onView(
                allOf(withId(R.id.encrypt_value_button), withText(R.string.button_encrypt_old_solution),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")),
                                        0),
                                1)))
        oldSolutionEncryptButton.perform(ViewActions.scrollTo(), click())

        val migrateToNewSolutionButton = onView(
                allOf(withId(R.id.migrate_value_button), withText(R.string.button_migrate),
                        childAtPosition(
                                childAtPosition(
                                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")),
                                        0),
                                3)))
        migrateToNewSolutionButton.perform(ViewActions.scrollTo(), click())
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}