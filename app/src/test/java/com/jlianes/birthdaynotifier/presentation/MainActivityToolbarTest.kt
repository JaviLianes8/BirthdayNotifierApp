package com.jlianes.birthdaynotifier.presentation

import android.content.Intent
import android.os.Build
import android.widget.ImageButton
import com.jlianes.birthdaynotifier.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Tests for the toolbar buttons in [MainActivity].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class MainActivityToolbarTest {

    private lateinit var activity: MainActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
    }

    @Test
    fun clickingLinkedin_opensLinkedinUrl() {
        val button = activity.findViewById<ImageButton>(R.id.buttonLinkedin)
        button.performClick()
        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://www.linkedin.com/in/jlianes/", intent.dataString)
    }

    @Test
    fun clickingBuyMeCoffee_opensCoffeeUrl() {
        val button = activity.findViewById<ImageButton>(R.id.buttonCoffee)
        button.performClick()
        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://buymeacoffee.com/jlianesglrs", intent.dataString)
    }

    @Test
    fun clickingGithub_opensRepoUrl() {
        val button = activity.findViewById<ImageButton>(R.id.buttonRepo)
        button.performClick()
        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://github.com/JaviLianes8/BirthdayNotifierApp", intent.dataString)
    }

    @Test
    fun clickingSettings_opensSettingsActivity() {
        val button = activity.findViewById<ImageButton>(R.id.buttonSettingsIcon)
        button.performClick()
        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(SettingsActivity::class.java.name, intent.component?.className)
    }
}
