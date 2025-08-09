package com.jlianes.birthdaynotifier.presentation

import android.os.Build
import android.widget.EditText
import android.widget.ListView
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.framework.file.BirthdayFileHelper
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLooper

/**
 * Tests for name filtering in [BirthdayListActivity].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BirthdayListNameFilterTest {

    private lateinit var controller: ActivityController<BirthdayListActivity>
    private lateinit var activity: BirthdayListActivity

    private val sampleData = listOf(
        JSONObject("""{"name":"Charlie","date":"10-03"}"""),
        JSONObject("""{"name":"Alice","date":"01-01"}"""),
        JSONObject("""{"name":"Bob","date":"05-06"}"""),
        JSONObject("""{"name":"Dave","date":"02-12"}"""),
    )

    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(BirthdayListActivity::class.java)
        activity = controller.get()

        val mockHelper = mockk<BirthdayFileHelper>()
        every { mockHelper.load() } returns Unit
        every { mockHelper.getAll() } returns sampleData

        val field = BirthdayListActivity::class.java.getDeclaredField("helper\$delegate")
        field.isAccessible = true
        field.set(activity, lazy { mockHelper })

        controller.setup()
    }

    private fun currentNames(): List<String> {
        val listView = activity.findViewById<ListView>(R.id.listView)
        val adapter = listView.adapter as BirthdayAdapter
        return (0 until adapter.count).map { adapter.getItem(it)!!.getString("name") }
    }

    @Test
    fun filterBySubstring() {
        val editText = activity.findViewById<EditText>(R.id.editFilter)
        editText.setText("a")
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val expected = listOf("Alice", "Charlie", "Dave")
        assertEquals(expected, currentNames())
    }

    @Test
    fun filterIsCaseInsensitive() {
        val editText = activity.findViewById<EditText>(R.id.editFilter)
        editText.setText("BO")
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val expected = listOf("Bob")
        assertEquals(expected, currentNames())
    }
}