package me.jameshunt.flow3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import me.jameshunt.flow.FragmentGroupFlowController
import me.jameshunt.flow.promise.always

class ViewPagerGroupController: FragmentGroupFlowController<Unit>(R.layout.group_view_pager) {

    var backIndex = 0

    override fun childIndexToDelegateBack(): Int = backIndex

    override fun setupGroup(layout: ViewGroup, flowsInGroup: FlowsInGroup<Unit>) {
        layout as ViewPager

        val inflater = LayoutInflater.from(layout.context)

        val pages = (0 until flowsInGroup.map.size).map { index ->
            when(index) {
                0 -> inflater.inflate(R.layout.group_view_pager_zero, layout, false)
                1 -> inflater.inflate(R.layout.group_view_pager_one, layout, false)
                2 -> inflater.inflate(R.layout.group_view_pager_two, layout, false)
                else -> throw NotImplementedError()
            }.also { layout.addView(it) }
        }

        layout.offscreenPageLimit = pages.size - 1

        layout.adapter = object: PagerAdapter() {
            override fun instantiateItem(collection: ViewGroup, position: Int): Any = pages[position]

            override fun getCount(): Int = pages.size

            override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                // no op
            }
        }

        layout.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                backIndex = position
            }
        })
    }
}