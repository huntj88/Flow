package me.jameshunt.flow3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.features.race
import me.jameshunt.flow.FragmentFlowController
import me.jameshunt.flow.FragmentGroupFlowController

class ViewPagerGroupController :
    FragmentGroupFlowController<ViewPagerGroupController.InternalInput, Unit>(R.layout.group_view_pager) {

    companion object {
        fun <A, B, C> input(
            pageZero: Class<A>,
            pageOne: Class<B>,
            pageTwo: Class<C>
        ): InternalInput
                where A : FragmentFlowController<Unit, Unit>,
                      B : FragmentFlowController<Unit, Unit>,
                      C : FragmentFlowController<Unit, Unit> {
            return InternalInput(
                pageZero = pageZero as Class<FragmentFlowController<Unit, Unit>>,
                pageOne = pageOne as Class<FragmentFlowController<Unit, Unit>>,
                pageTwo = pageTwo as Class<FragmentFlowController<Unit, Unit>>
            )
        }
    }

    data class InternalInput(
        val pageZero: Class<FragmentFlowController<Unit, Unit>>,
        val pageOne: Class<FragmentFlowController<Unit, Unit>>,
        val pageTwo: Class<FragmentFlowController<Unit, Unit>>
    ) : GroupInput()

    var backIndex = 0

    override fun childIndexToDelegateBack(): Int = backIndex

    override fun setupGroup(layout: ViewGroup) {
        layout as ViewPager

        val inflater = LayoutInflater.from(layout.context)

        val pages = (0..2).map { index ->
            when (index) {
                0 -> inflater.inflate(R.layout.group_view_pager_zero, layout, false)
                1 -> inflater.inflate(R.layout.group_view_pager_one, layout, false)
                2 -> inflater.inflate(R.layout.group_view_pager_two, layout, false)
                else -> throw NotImplementedError()
            }.also { layout.addView(it) }
        }

        layout.offscreenPageLimit = pages.size - 1

        layout.adapter = object : PagerAdapter() {
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

    override fun startFlowInGroup(groupInput: InternalInput): Promise<State> {
        val pageZero = this.flow(controller = groupInput.pageZero, viewId = R.id.groupPagerZero, input = Unit)
        val pageOne = this.flow(controller = groupInput.pageOne, viewId = R.id.groupPagerOne, input = Unit)
        val pageTwo = this.flow(controller = groupInput.pageTwo, viewId = R.id.groupPagerTwo, input = Unit)

        return race(pageZero, pageOne, pageTwo).forResult<Unit, State>(
            onBack = { Promise.value(Back) },
            onComplete = { Promise.value(Done(it)) }
        )
    }
}