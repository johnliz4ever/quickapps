package com.yoavst.quickapps.clock

import android.app.Fragment
import android.os.Handler
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import kotlin.properties.Delegates
import com.yoavst.quickapps.R
import java.util.concurrent.TimeUnit
import android.text.Html
import java.text.MessageFormat
import com.yoavst.quickapps.PrefManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import com.yoavst.kotlin.hide
import com.yoavst.kotlin.show
import com.yoavst.kotlin.postDelayed
import com.yoavst.kotlin.stringResource
/**
 * Created by Yoav.
 */
public open class ClockFragment : Fragment() {
    var handler: Handler = Handler()
    val showMillis by Delegates.lazy { PrefManager(getActivity()).stopwatchShowMillis().getOr(true) }
    val RESUME by stringResource(R.string.resume)
    val PAUSE by stringResource(R.string.pause)
    open val time: TextView by Delegates.lazy { getActivity().findViewById(R.id.time) as TextView }
    open val start: Button by Delegates.lazy { getActivity().findViewById(R.id.start) as Button }
    open val pause: Button by Delegates.lazy { getActivity().findViewById(R.id.pause) as Button }
    open val running: LinearLayout by Delegates.lazy { getActivity().findViewById(R.id.running) as LinearLayout }

    var callback: () -> Unit = {
        handler.post {
            val millis = StopwatchManager.getMillis()
            // Updating the UI
            val num = (millis % 1000 / 10).toInt()
            if (showMillis) {
                time.setText(Html.fromHtml(MessageFormat.format(TIME_FORMATTING,
                        getFromMilli(millis, TimeUnit.HOURS).format(),
                        getFromMilli(millis, TimeUnit.MINUTES).format(),
                        getFromMilli(millis, TimeUnit.SECONDS).format(),
                        num.format())))
            } else {
                time.setText(Html.fromHtml(MessageFormat.format(TIME_FORMATTING_NO_MILLIS, getFromMilli(millis, TimeUnit.HOURS).format(),
                        getFromMilli(millis, TimeUnit.MINUTES).format(),
                        getFromMilli(millis, TimeUnit.SECONDS).format())))
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stopwatch_circle_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start.setOnClickListener {
            setLookRunning()
            StopwatchManager.startTimer(10, callback)
        }
        pause.setOnClickListener {
            if (StopwatchManager.isRunning())
                StopwatchManager.PauseTimer()
            else
                StopwatchManager.ResumeTimer()
            setLookForPauseOrResume()
        }
        getActivity().findViewById(R.id.stop).setOnClickListener {
            StopwatchManager.stopTimer()
            handler.postDelayed(100) {
                running.hide()
                start.show()
                time.setText(Html.fromHtml(DEFAULT_STOPWATCH))
            }
        }
        time.setText(Html.fromHtml(if (showMillis) DEFAULT_STOPWATCH else DEFAULT_STOPWATCH_NO_MILLIS))
        handler = Handler()
        if (StopwatchManager.hasOldData()) {
            handler.postDelayed(100) {
                StopwatchManager.runOnUi(callback)
                setLookRunning()
                setLookForPauseOrResume()
            }
        }
    }

    fun setLookRunning() {
        start.hide()
        running.show()
        pause.setText(PAUSE)
    }

    fun setLookForPauseOrResume() {
        if (StopwatchManager.isRunning())
            pause.setText(PAUSE)
        else
            pause.setText(RESUME)
    }

    companion object {
        private val TIME_FORMATTING = "<big>{0}:{1}:{2}</big><small>.{3}</small>"
        private val TIME_FORMATTING_NO_MILLIS = "<big>{0}:{1}:{2}</big>"
        var DEFAULT_STOPWATCH = "<big>00:00:00</big><small>.00</small>"
        var DEFAULT_STOPWATCH_NO_MILLIS = "<big>00:00:00</big>"

        public fun getFromMilli(millis: Long, timeUnit: TimeUnit): Int {
            when (timeUnit) {
                TimeUnit.SECONDS -> // Number of seconds % 60
                    return (millis / 1000).toInt() % 60
                TimeUnit.MINUTES -> // Number of minutes % 60
                    return (millis / 60000).toInt() % 60
                TimeUnit.HOURS -> // Number of hours (can be more then 24)
                    return (millis / 1440000).toInt()
            }
            return 0
        }

        public fun Int.format(): String {
            return if (this < 10) "0" + this else Integer.toString(this)
        }
    }
}