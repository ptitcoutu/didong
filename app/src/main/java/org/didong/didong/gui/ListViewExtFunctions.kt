package org.didong.didong.gui

import android.widget.ExpandableListView
import android.view.View.MeasureSpec


/**
 * Extension functions on ListView
 */

fun ExpandableListView.setHeightBasedOnChildren() {
    val listAdapter = this.expandableListAdapter ?: // pre-condition
            return
    var totalHeight = 0
    this.measure(MeasureSpec.makeMeasureSpec(this.width, MeasureSpec.AT_MOST),
    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
    val desiredWidth = this.measuredWidth
    for (i in 0 until listAdapter.getGroupCount()) {
        val groupItem = listAdapter.getGroupView(i, false, null, this)

        groupItem.measure(MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

        // TODO : Find a way to remove the value 9 which is just a workaround to have the real height
        // Otherwise replace ExpandableListView with a RecyclerView
        totalHeight += groupItem.measuredHeight + 9 // 9 is just empirical value :-(

        if (this.isGroupExpanded(i)) {
            for (j in 0 until listAdapter.getChildrenCount(i)) {
                val listItem = listAdapter.getChildView(i, j, false, null,
                        this)
                listItem.measure(MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

                totalHeight += listItem.measuredHeight + 9 // 9 is just empirical value :-(

            }
        }
    }
    val params = this.getLayoutParams()
    val dividerCount = if (listAdapter.groupCount == 0) 0 else listAdapter.groupCount - 1
    params.height = totalHeight + this.dividerHeight * dividerCount
    this.layoutParams = params
    this.requestLayout()
}

fun ExpandableListView.expandFirstLevelChildren() {
    val listAdapter = this.getAdapter() ?: // pre-condition
            return
    for (i in 0 until listAdapter.count) {
        this.expandGroup(i)
    }
    this.setHeightBasedOnChildren()
}
