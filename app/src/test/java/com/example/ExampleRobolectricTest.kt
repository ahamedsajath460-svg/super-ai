package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.agent.CalculatorTool
import com.example.agent.DateTimeTool
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Super AI", appName)
    }

    @Test
    fun `test agent calculator tool evaluates arithmetic`() = runTest {
        val tool = CalculatorTool()
        val result = tool.execute("25 + 75")
        assertTrue(result.isSuccess)
        assertTrue(result.output.contains("100"))
    }

    @Test
    fun `test agent date time tool returns date info`() = runTest {
        val tool = DateTimeTool()
        val result = tool.execute("current time")
        assertTrue(result.isSuccess)
        assertTrue(result.output.contains("Current Date:"))
    }

    @Test
    fun `test agent gmail tool executes inbox action`() = runTest {
        val tool = com.example.agent.GmailTool("ahamedkky200@gmail.com")
        val result = tool.execute("check unread inbox emails")
        assertTrue(result.isSuccess)
        assertTrue(result.output.contains("ahamedkky200@gmail.com"))
    }
}
