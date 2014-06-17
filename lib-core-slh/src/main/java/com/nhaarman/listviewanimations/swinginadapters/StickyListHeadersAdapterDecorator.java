package com.nhaarman.listviewanimations.swinginadapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.util.AnimatorUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nhaarman.listviewanimations.util.StickyListHeadersListViewWrapper;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A {@link com.nhaarman.listviewanimations.BaseAdapterDecorator} which can be used to animate header views provided by a
 * {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter}.
 */
public class StickyListHeadersAdapterDecorator extends BaseAdapterDecorator<StickyListHeadersListView> implements StickyListHeadersAdapter {

    /**
     * Alpha property.
     */
    private static final String ALPHA = "alpha";

    /**
     * The decorated {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter}.
     */
    @NonNull
    private final StickyListHeadersAdapter mStickyListHeadersAdapter;

    /**
     * The {@link com.nhaarman.listviewanimations.swinginadapters.ViewAnimator} responsible for animating the Views.
     */
    @Nullable
    private ViewAnimator<StickyListHeadersListView> mViewAnimator;

    /**
     * Create a new {@code StickyListHeadersAdapterDecorator}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter the {@code BaseAdapter} to decorate. If this is a {@code BaseAdapterDecorator}, it should wrap an instance of
     *                    {@link se.emilsjolander.stickylistheaders.StickyListHeadersAdapter}.
     */
    public StickyListHeadersAdapterDecorator(@NonNull final BaseAdapter baseAdapter) {
        super(baseAdapter);

        BaseAdapter adapter = baseAdapter;
        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator<StickyListHeadersListView>) adapter).getDecoratedBaseAdapter();
        }

        if (!(adapter instanceof StickyListHeadersAdapter)) {
            throw new IllegalArgumentException(adapter.getClass().getCanonicalName() + " does not implement StickyListHeadersAdapter");
        }

        mStickyListHeadersAdapter = (StickyListHeadersAdapter) adapter;
    }

    /**
     * @deprecated use {@link #setStickyListHeadersListView(se.emilsjolander.stickylistheaders.StickyListHeadersListView)} instead.
     */
    @Override
    @Deprecated
    public void setAbsListView(@NonNull final AbsListView absListView) {
        super.setAbsListView(absListView);
    }

    /**
     * Sets the {@link se.emilsjolander.stickylistheaders.StickyListHeadersListView} that this adapter will be bound to.
     */
    public void setStickyListHeadersListView(@NonNull final StickyListHeadersListView listView) {
        ListViewWrapper<StickyListHeadersListView> stickyListHeadersListViewWrapper = new StickyListHeadersListViewWrapper(listView);
        setListViewWrapper(stickyListHeadersListViewWrapper);
    }

    /**
     * Returns the {@link com.nhaarman.listviewanimations.swinginadapters.ViewAnimator} responsible for animating the header Views in this adapter.
     */
    @Nullable
    public ViewAnimator<StickyListHeadersListView> getViewAnimator() {
        return mViewAnimator;
    }

    @Override
    public void setListViewWrapper(@NonNull final ListViewWrapper<StickyListHeadersListView> listViewWrapper) {
        super.setListViewWrapper(listViewWrapper);
        mViewAnimator = new ViewAnimator<>(listViewWrapper);
    }

    @Override
    public View getHeaderView(final int position, final View convertView, final ViewGroup parent) {
        if (getListViewWrapper() == null) {
            throw new IllegalStateException("Call setStickyListHeadersListView() on this AnimationAdapter first!");
        }

        if (convertView != null) {
            assert mViewAnimator != null;
            mViewAnimator.cancelExistingAnimation(convertView);
        }

        View itemView = mStickyListHeadersAdapter.getHeaderView(position, convertView, parent);

        animateViewIfNecessary(position, itemView, parent);
        return itemView;
    }

    /**
     * Animates given View if necessary.
     *
     * @param position the position of the item the View represents.
     * @param view     the View that should be animated.
     * @param parent   the parent the View is hosted in.
     */
    private void animateViewIfNecessary(final int position, @NonNull final View view, @NonNull final ViewGroup parent) {
        Animator[] childAnimators;
        if (getDecoratedBaseAdapter() instanceof AnimationAdapter) {
            childAnimators = ((AnimationAdapter<StickyListHeadersListView>) getDecoratedBaseAdapter()).getAnimators(parent, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator alphaAnimator = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

        assert mViewAnimator != null;
        mViewAnimator.animateViewIfNecessary(position, view, AnimatorUtil.concatAnimators(childAnimators, new Animator[0], alphaAnimator));
    }

    @Override
    public long getHeaderId(final int position) {
        return mStickyListHeadersAdapter.getHeaderId(position);
    }
}