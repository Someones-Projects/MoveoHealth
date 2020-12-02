package com.example.moveohealth.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TopSpacingItemDecoration(
    private val itemMargin: Int,
    private val columns: Int = 1
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildLayoutPosition(view)

        outRect.left = itemMargin
        outRect.right = itemMargin
        outRect.bottom = itemMargin
        outRect.top =
            if (position < columns) itemMargin * 4 else itemMargin //first row get more margin

    }
}