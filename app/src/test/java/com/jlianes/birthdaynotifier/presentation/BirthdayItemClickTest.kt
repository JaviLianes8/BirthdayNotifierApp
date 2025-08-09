package com.jlianes.birthdaynotifier.presentation

import android.os.Build
import android.widget.ListView
import com.jlianes.birthdaynotifier.R
import com.jlianes.birthdaynotifier.framework.file.BirthdayFileHelper
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.Shadows

/**
 * Verifies that clicking on a birthday entry opens the edit dialog.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BirthdayItemClickTest {

    private lateinit var controller: ActivityController<BirthdayListActivity>
    private lateinit var activity: BirthdayListActivity

    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(BirthdayListActivity::class.java)
        activity = controller.get()

        val mockHelper = mockk<BirthdayFileHelper>()
        val sample = JSONObject("""{"name":"Alice","date":"01-01","phone":"+111111111","message":"Hello"}""")
        every { mockHelper.load() } returns Unit
        every { mockHelper.getAll() } returns listOf(sample)
        every { mockHelper.get(0) } returns sample

        val field = BirthdayListActivity::class.java.getDeclaredField("helper\$delegate")
        field.isAccessible = true
        field.set(activity, lazy { mockHelper })

        controller.setup()
    }

    @Test
    fun clickingListItem_showsEditDialog() {
        val listView = activity.findViewById<ListView>(R.id.listView)
        val itemView = listView.adapter.getView(0, null, listView)
        listView.performItemClick(itemView, 0, listView.adapter.getItemId(0))

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull(dialog)
        val shadowDialog = Shadows.shadowOf(dialog)
        assertEquals(activity.getString(R.string.edit_birthday), shadowDialog.title)
        assertTrue(dialog!!.isShowing)
    }
}

