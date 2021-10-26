package dora.widget

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class MainActivity : AppCompatActivity() {

    lateinit var tabBar: DoraTabBar
    var isScrolling: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tabBar = findViewById(R.id.tabBar)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        tabBar.setTitles(arrayOf("推荐", "热点", "电视剧", "电影", "综艺"))
        tabBar.addTextTab("添加的频道1")
        tabBar.addTextTab("添加的频道2")
        tabBar.addTextTab("添加的频道3")
        tabBar.addTextTab("添加的频道4")
        tabBar.addTextTab("添加的频道5")
        tabBar.setOnTabClickListener(object : DoraTabBar.OnTabClickListener {

            override fun onTabClick(view: View, position: Int) {
                viewPager.currentItem = position
            }
        })
        viewPager.adapter = TabPageAdapter()
        viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (isScrolling) {
                    tabBar.offsetTab(position, positionOffset)
                }
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    isScrolling = false
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    isScrolling = true
                }
            }
        })
    }

    inner class TabPageAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return tabBar.tabCount
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val textView = TextView(this@MainActivity)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            textView.gravity = Gravity.CENTER
            textView.text = tabBar.tabTitles[position]
            container.addView(textView)
            return textView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}