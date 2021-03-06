package com.teambartender3.filters.FilterList.HorizontalAdapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.teambartender3.filters.R;

/**
 * Created by samer on 2018-03-16.
 */

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;

    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return viewHolder.getAdapterPosition() == 0 || viewHolder.getItemViewType() == R.layout.layout_filter_list_end_btt ? 0 : makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                ItemTouchHelperViewHolder itemViewHolder =
                        (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemSelected(viewHolder.getAdapterPosition());
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        mAdapter.onItemMoveFinished(fromPos, toPos);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            ItemTouchHelperViewHolder itemViewHolder =
                    (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear(viewHolder.getAdapterPosition());
        }
    }
}
