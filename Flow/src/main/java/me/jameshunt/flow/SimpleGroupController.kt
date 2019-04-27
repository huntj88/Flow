package me.jameshunt.flow

class SimpleGroupController: FragmentGroupFlowController<Unit>(R.layout.group_simple)

class DeepLinkGroupController(val deepLinkData: DeepLinkData): FragmentGroupFlowController<Unit>(R.layout.group_simple)