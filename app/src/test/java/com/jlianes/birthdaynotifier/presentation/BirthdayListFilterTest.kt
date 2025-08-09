package com.jlianes.birthdaynotifier.presentation

import android.os.Build
import android.widget.ListView
import android.widget.Spinner
import io.mockk.every
import io.mockk.mockk
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.framework.file.BirthdayFileHelper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.android.controller.ActivityController

/**
 * Tests for the sorting filters in [BirthdayListActivity].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BirthdayListActivityFilterTest {

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
    fun sortByDateAscending() {
        val expected = listOf("Alice", "Charlie", "Bob", "Dave")
        assertEquals(expected, currentNames())
    }

    @Test
    fun sortByDateDescending() {
        val spinner = activity.findViewById<Spinner>(R.id.spinnerSort)
        spinner.setSelection(1)
        val expected = listOf("Dave", "Bob", "Charlie", "Alice")
        assertEquals(expected, currentNames())
    }

    @Test
    fun sortByNameAscending() {
        val spinner = activity.findViewById<Spinner>(R.id.spinnerSort)
        spinner.setSelection(2)
        val expected = listOf("Alice", "Bob", "Charlie", "Dave")
        assertEquals(expected, currentNames())
    }

    @Test
    fun sortByNameDescending() {
        val spinner = activity.findViewById<Spinner>(R.id.spinnerSort)
        spinner.setSelection(3)
        val expected = listOf("Dave", "Charlie", "Bob", "Alice")
        assertEquals(expected, currentNames())
    }
}