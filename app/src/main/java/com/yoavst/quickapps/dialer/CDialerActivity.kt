package com.yoavst.quickapps.dialer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.lge.qcircle.template.*
import com.yoavst.kotlin.colorRes
import com.yoavst.kotlin.hide
import com.yoavst.kotlin.show
import com.yoavst.kotlin.telephonyManager
import com.yoavst.quickapps.R
import com.yoavst.quickapps.util.QCircleActivity
import kotlin.properties.Delegates

/**
 * Created by Yoav.
 */
public class CDialerActivity : QCircleActivity() {
    override val template: QCircleTemplate by Delegates.lazy { QCircleTemplate(this, TemplateType.CIRCLE_EMPTY) }
    var hasInitButtons = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val main = template.getLayoutById(TemplateTag.CONTENT_MAIN)
        val pager = ViewPager(this)
        pager.setId(R.id.pager)
        pager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    if (hasInitButtons) hideButtonAndTitle()
                } else {
                    if (!hasInitButtons) {
                        template.addElement(QCircleBackButton(this@CDialerActivity))
                        val title = QCircleTitle(this@CDialerActivity, getString(R.string.contacts), Color.WHITE, colorRes(R.color.md_blue_700))
                        title.setTextSize(17F)
                        template.addElement(title)
                        hasInitButtons = true
                    } else {
                        template.getView().findViewById(R.id.backButton).show()
                        template.getView().findViewById(R.id.title).show()
                    }
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })

        pager.setAdapter(DialerAdapter(getFragmentManager()))
        main.addView(pager)
        setContentView(template.getView())

    }

    fun hideButtonAndTitle() {
        template.getView().findViewById(R.id.backButton).hide()
        template.getView().findViewById(R.id.title).hide()
    }

    override fun getIntentToShow(): Intent? {
        return Intent().setClassName("com.android.contacts", "alias.PeopleFloatingActivity").putExtra("com.lge.app.floating.launchAsFloating", true)
    }

    companion object {
        public fun GetCountryZipCode(context: Context): String {
            var CountryZipCode = ""
            val manager = context.telephonyManager()
            var CountryID = manager.getSimCountryIso().toUpperCase().trim()
            val rl = context.getResources().getStringArray(R.array.CountryCodes)
            for (aRl in rl) {
                val g = aRl.split(",")
                if (g[1].trim() == CountryID) {
                    CountryZipCode = g[0]
                    break
                }
            }
            return CountryZipCode
        }
    }

}