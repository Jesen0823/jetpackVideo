package com.jesen.cod.jetpackvideo.ui.mine

import android.os.Bundle
import android.view.View
import androidx.paging.PagedListAdapter
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayDetector
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager
import com.jesen.cod.jetpackvideo.model.Feed
import com.jesen.cod.jetpackvideo.ui.home.FeedAdapter
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout


class FavoriteHistoryFragment : AbsListFragment<Feed, FavoriteHistoryViewModel>() {

    private val category = "user_behavior_fav_his"

    private var shouldPause = true

    private lateinit var playDetector: PageListPlayDetector

    companion object {
        fun newInstance(behaviorType: Int): FavoriteHistoryFragment {
            val args = Bundle()
            args.putInt(UserFavoriteHistoryActivity.KEY_BEHAVIOR_TYPE, behaviorType)
            val fragment = FavoriteHistoryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playDetector = PageListPlayDetector(this, mRecyclerView)
        val behavior = arguments?.getInt(UserFavoriteHistoryActivity.KEY_BEHAVIOR_TYPE)
        if (behavior != null) {
            mViewModel.setBehavior(behavior)
        }
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        mViewModel.dataSource.invalidate()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        val currentList = mAdapter.currentList
        finishRefresh(currentList != null && currentList.size > 0)
    }

    override fun getAdapter(): PagedListAdapter<*, *>? {
        return object :
            FeedAdapter(context, category) {
            override fun onViewAttachedToWindow2(holder: ViewHolder) {
                if (holder.isVideoItem) {
                    playDetector.addTarget(holder.listPlayerView)
                }
            }

            override fun onViewDetachedFromWindow2(holder: ViewHolder) {
                if (holder.isVideoItem) {
                    playDetector.removeTarget(holder.listPlayerView)
                }
            }

            override fun onStartFeedDetailActivity(feed: Feed?) {
                shouldPause = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (shouldPause) playDetector.onPause()
    }

    override fun onResume() {
        super.onResume()
        shouldPause = true
        playDetector.onResume()
    }

    override fun onDestroy() {
        PageListPlayManager.removePageListPlay(category)
        super.onDestroy()
    }
}