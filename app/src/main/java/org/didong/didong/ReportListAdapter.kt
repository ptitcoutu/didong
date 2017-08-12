package org.didong.didong

import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.graphics.Typeface
import android.widget.TextView
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater


/**
 * Adapter which agregate event by tags for a day
 */
class ReportListAdapter(val activityByTag: Map<String, Long>) : BaseExpandableListAdapter() {
    val tags : List<String>

    init {
        tags = activityByTag.keys.toList()
    }

    /**
     * Gets the data associated with the given group.

     * @param groupPosition the position of the group
     * *
     * @return the data child for the specified group
     */
    override fun getGroup(groupPosition: Int): Any = tags[groupPosition]

    /**
     * Whether the child at the specified position is selectable.

     * @param groupPosition the position of the group that contains the child
     * *
     * @param childPosition the position of the child within the group
     * *
     * @return whether the child is selectable.
     */
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    /**
     * Indicates whether the child and group IDs are stable across changes to the
     * underlying data.

     * @return whether or not the same ID always refers to the same object
     * *
     * @see Adapter.hasStableIds
     */
    override fun hasStableIds(): Boolean = true

    /**
     * Gets a View that displays the given group. This View is only for the
     * group--the Views for the group's children will be fetched using
     * [.getChildView].

     * @param groupPosition the position of the group for which the View is
     * *            returned
     * *
     * @param isExpanded whether the group is expanded or collapsed
     * *
     * @param convertView the old view to reuse, if possible. You should check
     * *            that this view is non-null and of an appropriate type before
     * *            using. If it is not possible to convert this view to display
     * *            the correct data, this method can create a new view. It is not
     * *            guaranteed that the convertView will have been previously
     * *            created by
     * *            [.getGroupView].
     * *
     * @param parent the parent that this view will eventually be attached to
     * *
     * @return the View corresponding to the group at the specified position
     */
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupTitleContent = getGroup(groupPosition) as String
        val groupView = LayoutInflater.from(parent!!.context).inflate(R.layout.report_group, null)

        val groupTitle = groupView
                .findViewById(R.id.groupTitle) as TextView
        groupTitle.setTypeface(null, Typeface.BOLD)
        groupTitle.text = "       ${groupTitleContent}"

        return groupView
    }

    /**
     * Gets the number of children in a specified group.

     * @param groupPosition the position of the group for which the children
     * *            count should be returned
     * *
     * @return the children count in the specified group
     */
    override fun getChildrenCount(groupPosition: Int): Int = 1

    /**
     * Gets the data associated with the given child within the given group.

     * @param groupPosition the position of the group that the child resides in
     * *
     * @param childPosition the position of the child with respect to other
     * *            children in the group
     * *
     * @return the data of the child
     */
    override fun getChild(groupPosition: Int, childPosition: Int): Any = activityByTag[tags[groupPosition]] ?: 0

    /**
     * Gets the ID for the group at the given position. This group ID must be
     * unique across groups. The combined ID (see
     * [.getCombinedGroupId]) must be unique across ALL items
     * (groups and all children).

     * @param groupPosition the position of the group for which the ID is wanted
     * *
     * @return the ID associated with the group
     */
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    /**
     * Gets a View that displays the data for the given child within the given
     * group.

     * @param groupPosition the position of the group that contains the child
     * *
     * @param childPosition the position of the child (for which the View is
     * *            returned) within the group
     * *
     * @param isLastChild Whether the child is the last child within the group
     * *
     * @param convertView the old view to reuse, if possible. You should check
     * *            that this view is non-null and of an appropriate type before
     * *            using. If it is not possible to convert this view to display
     * *            the correct data, this method can create a new view. It is not
     * *            guaranteed that the convertView will have been previously
     * *            created by
     * *            [.getChildView].
     * *
     * @param parent the parent that this view will eventually be attached to
     * *
     * @return the View corresponding to the child at the specified position
     */
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val reportItemContent = getChild(groupPosition, childPosition) as Long
        val reportItemView = LayoutInflater.from(parent!!.context).inflate(R.layout.report_item, null)

        val itemDetail = reportItemView
                .findViewById(R.id.itemDetail) as TextView
        val totalSeconds = reportItemContent.toDouble() / 1000
        val totalMinutes = totalSeconds / 60
        val totalHours = totalMinutes / 60
        val totalDays = totalHours / 8
        itemDetail.text = "${totalSeconds.format(0)} s  / ${totalMinutes.format(2)} mn / ${totalHours.format(2)} h / ${totalDays.format(2)} d"
        return reportItemView
    }

    /**
     * Gets the ID for the given child within the given group. This ID must be
     * unique across all children within the group. The combined ID (see
     * [.getCombinedChildId]) must be unique across ALL items
     * (groups and all children).

     * @param groupPosition the position of the group that contains the child
     * *
     * @param childPosition the position of the child within the group for which
     * *            the ID is wanted
     * *
     * @return the ID associated with the child
     */
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition.toLong() + childPosition * 100)

    /**
     * Gets the number of groups.

     * @return the number of groups
     */
    override fun getGroupCount(): Int = tags.size

}