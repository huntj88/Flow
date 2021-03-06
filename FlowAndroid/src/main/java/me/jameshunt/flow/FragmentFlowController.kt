package me.jameshunt.flow

import com.inmotionsoftware.promisekt.*

typealias ViewId = Int

interface AndroidFlowFunctions {
    fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>>
            where Controller : FragmentFlowController<NewInput, NewOutput>

    fun <FragInput, FragOutput, FragmentType> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>>
            where FragmentType : FlowUI<FragInput, FragOutput>

    fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>>
            where Controller : FragmentGroupFlowController<GroupInput, GroupOutput>

    fun <NewInput, NewOutput, Controller> flowBusiness(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>>
            where Controller : BusinessFlowController<NewInput, NewOutput>
}

abstract class FragmentFlowController<Input, Output> : AndroidFlowController<Input, Output>() {

    internal var viewId: ViewId = 0 // is only set once at the beginning

    private val flowFunctions: AndroidFlowFunctions = AndroidFlowFunctionsImpl()

    private var activeFragment: FragmentProxy<*, *, *>? = null

    private var activeDialogFragment: FragmentProxy<*, *, *>? = null

    internal var uncommittedTransaction: (() -> Unit)? = null
        private set

    protected fun <FragInput, FragOutput, FragmentType> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>>
            where FragmentType : FlowUI<FragInput, FragOutput> {
        return flowFunctions.flow(fragmentProxy = fragmentProxy, input = input)
    }

    protected fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>>
            where Controller : FragmentFlowController<NewInput, NewOutput> {
        return flowFunctions.flow(controller = controller, input = input)
    }

    protected fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>>
            where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {
        return flowFunctions.flowGroup(controller = controller, input = input)
    }

    protected fun <NewInput, NewOutput, Controller> flowBusiness(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>>
            where Controller : BusinessFlowController<NewInput, NewOutput> {
        return flowFunctions.flowBusiness(controller = controller, input = input)
    }

    /**
     * resuming only renders the view again
     * the fragment reattaches itself to the existing promise
     */

    final override fun resume() {
        (activeFragment as? FragmentProxy<Any?, Any?, FlowFragment<Any?, Any?>>)?.let {
            fun showFragment() = FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )

            try {
                showFragment()
            } catch (e: IllegalStateException) {
                e.printStackTrace() // from committing transaction after onSavedInstanceState
                uncommittedTransaction = { showFragment(); uncommittedTransaction = null }
            }
        }

        (activeDialogFragment as? FragmentProxy<Any?, Any?, FlowDialogFragment<Any?, Any?>>)?.let {
            // DialogFragments are kept around in FragmentManager memory longer,
            // so null out the instance so it won't try and show the instance from the old activity
            it.fragment = null

            fun showDialogFragment() = FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )

            try {
                showDialogFragment()
            } catch (e: IllegalStateException) {
                e.printStackTrace() // from committing transaction after onSavedInstanceState
                uncommittedTransaction = {
                    uncommittedTransaction?.invoke() // show the fragment behind dialog
                    showDialogFragment()
                    uncommittedTransaction = null
                }
            }
        }
    }

    final override fun handleBack() {
        this.childFlows.firstOrNull()
            .let {
                when (it) {
                    is AndroidFlowController<*, *> -> {
                        activeFragment = null
                        it.handleBack()
                    }
                    is BusinessFlowController<*, *> -> businessDeferred.resolve(FlowResult.Back)
                    else -> this.activeFragment?.back()
                }
            }
    }

    // promise wrapper around business flow to handle android back, even though BusinessFlowControllers
    // don't have a concept of going back
    private var businessDeferred: DeferredPromise<FlowResult<Any?>> = DeferredPromise()

    private inner class AndroidFlowFunctionsImpl : AndroidFlowFunctions {

        override fun <NewInput, NewOutput, Controller> flow(
            controller: Class<Controller>,
            input: NewInput
        ): Promise<FlowResult<NewOutput>>
                where Controller : FragmentFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance().apply {
                // apply same viewId to child
                this@apply.viewId = this@FragmentFlowController.viewId
            }

            childFlows.add(flowController)

            return flowController.launchFlow(input).ensure {
                childFlows.remove(flowController)
            }
        }

        override fun <FragInput, FragOutput, FragmentType> flow(
            fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
            input: FragInput
        ): Promise<FlowResult<FragOutput>>
                where FragmentType : FlowUI<FragInput, FragOutput> {

            val isDialog = FlowDialogFragment::class.java.isAssignableFrom(fragmentProxy.clazz)

            when (isDialog) {
                true -> {
                    activeDialogFragment = fragmentProxy.also { it.input = input }
                    activeFragment = FlowManager.fragmentDisplayManager.getVisibleFragmentProxy(viewId)
                }
                false -> activeFragment = fragmentProxy.also { it.input = input }
            }

            fun showFragmentForResult(): Promise<FlowResult<FragOutput>> = FlowManager
                .fragmentDisplayManager
                .show(fragmentProxy = fragmentProxy, viewId = this@FragmentFlowController.viewId)
                .flowForResult()
                .ensure { activeDialogFragment = null }

            return try {
                when (FlowManager.rootViewManager.isViewVisible(viewId)) {
                    true -> showFragmentForResult()
                    false -> {
                        val fragmentName = fragmentProxy.clazz.simpleName
                        val flowName = this::class.java.simpleName
                        val message = "View does not exist for fragment: $fragmentName, in flow: $flowName"
                        throw IllegalStateException(message)
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                // from committing transaction after onSavedInstanceState,
                // or view does not exist
                uncommittedTransaction = {
                    showFragmentForResult().ensure { uncommittedTransaction = null }
                }

                fragmentProxy.deferredPromise.promise
            }
        }

        override fun <GroupInput, GroupOutput, Controller> flowGroup(
            controller: Class<Controller>,
            input: GroupInput
        ): Promise<FlowResult<GroupOutput>>
                where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {

            // remove all the fragments from this flowController before starting the next FlowController
            // (state will still be saved when they get back)
            // The fragments parent views could potentially no longer exist
            FlowManager.fragmentDisplayManager.removeAll()

            val flowController = controller.newInstance()

            childFlows.add(flowController)

            return flowController.launchFlow(input).ensure {
                childFlows.remove(flowController)
                FlowManager.resumeActiveFlowControllers()
            }
        }

        override fun <NewInput, NewOutput, Controller> flowBusiness(
            controller: Class<Controller>,
            input: NewInput
        ): Promise<FlowResult<NewOutput>>
                where Controller : BusinessFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance()
            childFlows.add(flowController)

            // businessDeferred is resolvable outside of newly launched flow to handle android back
            flowController.launchFlow(input)
                .done { businessDeferred.resolve(FlowResult.Completed(it)) }
                .catch { businessDeferred.reject(it) }

            return businessDeferred.promise.ensure {
                childFlows.remove(flowController)
                businessDeferred = DeferredPromise()
            } as Promise<FlowResult<NewOutput>>
        }
    }
}