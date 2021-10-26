package dora.widget

import android.content.Context
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.graphics.*
import android.widget.TextView
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.View

class DoraTabBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    var position = 0
    set(position) {
        if (position != field) {
            field = position
            updateSelectedTab()
        }
    }
    private var positionOffset = 0f
        private set
    private lateinit var wrappedTabLayoutParams: LinearLayout.LayoutParams
    private lateinit var divideTabLayoutParams: LinearLayout.LayoutParams
    private var tabContainer: LinearLayout = LinearLayout(context)
    val tabCount : Int
        get() = tabContainer.childCount
    private var tabTextSize = 15f
    private var tabTextColor = Color.BLACK
    private var indicatorHeight = 10
    private var indicatorColor = Color.BLACK
    private var tabHorizontalPadding = 10
    private var tabVerticalPadding = 10
    private var tabColor = Color.TRANSPARENT
    private var selectedTabTextColor = 0
        private set
    private var textAllCaps = true
    private var rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var onTabClickListener: OnTabClickListener? = null
    var tabTitles: MutableList<String> = arrayListOf()
    private var tabScrollX = 0
    private var indicatorRect  = RectF()

    private var isDivide: Boolean = false
        set(isDivide) {
            if (isDivide != field) {
                field = isDivide
            }
        }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        isFillViewport = true
        isHorizontalScrollBarEnabled = false
        divideTabLayoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
        )
        wrappedTabLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rectPaint.style = Paint.Style.FILL
        tabContainer.layoutParams = LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        tabContainer.orientation = LinearLayout.HORIZONTAL
        addView(tabContainer)
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DoraTabBar, defStyleAttr, 0)
        indicatorColor = a.getColor(R.styleable.DoraTabBar_dora_indicatorColor, resources.getColor(R.color.teal_200))
        indicatorHeight = a.getDimensionPixelSize(R.styleable.DoraTabBar_dora_indicatorHeight, indicatorHeight)
        tabHorizontalPadding = a.getDimensionPixelSize(R.styleable.DoraTabBar_dora_tabHorizontalPadding, tabHorizontalPadding)
        tabVerticalPadding = a.getDimensionPixelSize(R.styleable.DoraTabBar_dora_tabVerticalPadding, tabVerticalPadding)
        tabColor = a.getColor(
            R.styleable.DoraTabBar_dora_tabColor,
            tabColor
        )
        tabTextColor = a.getColor(R.styleable.DoraTabBar_dora_tabTextColor, tabTextColor)
        selectedTabTextColor = a.getColor(
            R.styleable.DoraTabBar_dora_tabSelectedTextColor,
            resources.getColor(R.color.teal_200)
        )
        tabTextSize = a.getDimension(
            R.styleable.DoraTabBar_dora_tabTextSize, tabTextSize
        )
        isDivide = a.getBoolean(R.styleable.DoraTabBar_dora_isDivide, isDivide)
        textAllCaps = a.getBoolean(R.styleable.DoraTabBar_dora_textAllCaps, textAllCaps)
        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        drawIndicator(canvas)
    }

    private fun drawIndicator(canvas: Canvas) {
        rectPaint.color = indicatorColor
        val currentTab = tabContainer.getChildAt(position)
        if (currentTab != null) {
            var left = currentTab.left.toFloat()
            var right = currentTab.right.toFloat()
            if (positionOffset > 0f && position < tabCount - 1) {
                val nextTab = tabContainer.getChildAt(position + 1)
                val nextTabLeft = nextTab.left.toFloat()
                val nextTabRight = nextTab.right.toFloat()
                left = positionOffset * nextTabLeft + (1f - positionOffset) * left
                right = positionOffset * nextTabRight + (1f - positionOffset) * right
            }
            indicatorRect[left, (height - indicatorHeight).toFloat(), right] =
                    height.toFloat()
            canvas.drawRect(indicatorRect, rectPaint)
        }
    }

    fun setTitles(titles: Array<String>) {
        for (title in titles) {
            addTextTab(title)
        }
    }

    interface OnTabClickListener {
        fun onTabClick(view: View, position: Int)
    }

    fun setOnTabClickListener(l: OnTabClickListener) {
        onTabClickListener = l
    }

    /**
     * 通常使用在跟viewPager联动。
     */
    fun offsetTab(position: Int, positionOffset: Float) {
        if (position != this.position || positionOffset != this.positionOffset) {
            this.position = position
            this.positionOffset = positionOffset
            updateSelectedTab()
        }
    }

    private fun scrollToChild(position: Int, offsetPixels: Int) {
        if (tabCount == 0) {
            return
        }
        val scrollOffset = (width - getTabWidth(position)) / 2
        var scrollX = getTabLeft(position) + offsetPixels
        if (position > 0 || offsetPixels > 0) {
            scrollX -= scrollOffset
        }
        if (scrollX != this.tabScrollX) {
            this.tabScrollX = scrollX
            smoothScrollTo(scrollX, 0)
        }
    }

    fun getTabView(position: Int): View {
        return tabContainer.getChildAt(position)
    }

    fun getTabWidth(position: Int): Int {
        return getTabView(position).width
    }

    fun getTabLeft(position: Int): Int {
        return getTabView(position).left
    }

    fun getTabRight(position: Int): Int {
        return getTabView(position).right
    }

    val tabContainerWidth: Int
        get() = tabContainer.width

    private fun invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    fun updateSelectedTab() {
        for (index in 0 until tabTitles.size) {
            val tabView = getTabView(index) as TextView
            if (index == position) {
                tabView.setTextColor(selectedTabTextColor)
            } else {
                tabView.setTextColor(tabTextColor)
            }
        }
        scrollToChild(position, (positionOffset * getTabWidth(position)).toInt())
        invalidateView()
    }

    fun addTextTab(title: String) {
        addTextTab(tabTitles.size, title)
    }

    fun addTextTab(position: Int, title: String) {
        val textTab = TextView(context)
        textTab.text = title
        if (this.position == position) {
            textTab.setTextColor(selectedTabTextColor)
        } else {
            textTab.setTextColor(tabTextColor)
        }
        textTab.textSize = tabTextSize
        textTab.gravity = Gravity.CENTER
        textTab.setSingleLine()
        textTab.isFocusable = true
        textTab.setPaddingRelative(tabHorizontalPadding, tabVerticalPadding, tabHorizontalPadding, tabVerticalPadding)
        textTab.setOnClickListener { v ->
            this.position = position
            updateSelectedTab()
            onTabClickListener?.onTabClick(v, position)
        }
        tabContainer.addView(
                textTab,
                position,
                if (isDivide) divideTabLayoutParams else wrappedTabLayoutParams
        )
        tabTitles.add(title)
    }

    init {
        init(context, attrs, defStyleAttr)
    }
}