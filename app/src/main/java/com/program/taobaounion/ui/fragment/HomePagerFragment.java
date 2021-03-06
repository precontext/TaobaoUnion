package com.program.taobaounion.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.views.TbNestedScrollView;
import com.program.taobaounion.R;
import com.program.taobaounion.base.BaseFragment;
import com.program.taobaounion.model.domain.Categories;
import com.program.taobaounion.model.domain.HomePagerContent;
import com.program.taobaounion.model.domain.IBaseInfo;
import com.program.taobaounion.presenter.ICateGoryPagerPresenter;
import com.program.taobaounion.ui.adapter.LinearItemContentAdapter;
import com.program.taobaounion.ui.adapter.LooperPagerAdapter;
import com.program.taobaounion.ui.custom.AutoLoopViewPager;
import com.program.taobaounion.utils.Constants;
import com.program.taobaounion.utils.LogUtils;
import com.program.taobaounion.utils.PresenterManager;
import com.program.taobaounion.utils.SizeUtils;
import com.program.taobaounion.utils.TicketUtil;
import com.program.taobaounion.utils.ToastUtils;
import com.program.taobaounion.view.ICategoryPagerCallback;

import java.util.List;

import butterknife.BindView;

public class HomePagerFragment extends BaseFragment implements ICategoryPagerCallback, LinearItemContentAdapter.OnListItemClickListener, LooperPagerAdapter.OnLoopPagerItemClickListener {

    private ICateGoryPagerPresenter mPagerPresenter;
    private int mMaterialId;


    @BindView(R.id.home_pager_content_list)
    public RecyclerView mContentList;
    private LinearItemContentAdapter mContentAdapter;

    @BindView(R.id.looper_pager)
    public AutoLoopViewPager looperPager;
    private LooperPagerAdapter mLooperPagerAdapter;

    @BindView(R.id.home_pager_title)
    public TextView currentCategoryTitleTv;

    @BindView(R.id.looper_point_container)
    public LinearLayout looperPointContainer;

    @BindView(R.id.home_pager_refresh)
    public TwinklingRefreshLayout mTwinklingRefreshLayout;
    @BindView(R.id.home_pager_parent)
    public LinearLayout homePagerParent ;
    @BindView(R.id.home_pager_nest_scroll)
    public TbNestedScrollView homePagerNestedView ;

    @BindView(R.id.home_pager_header_container)
    public LinearLayout homeHeaderContainer ;

    public static HomePagerFragment newInstance(Categories.DataBean category) {
        HomePagerFragment homePagerFragment = new HomePagerFragment();
        //
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_HOME_PAGER_TITLE, category.getTitle());
        bundle.putInt(Constants.KEY_HOME_PAGER_MATERIAL_ID, category.getId());
        homePagerFragment.setArguments(bundle);
        return homePagerFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        //?????????????????????
        looperPager.startLoop();
    }

    @Override
    public void onPause() {
        super.onPause();
        //??????????????????
        looperPager.stopLoop();
    }

    @Override
    protected int getRootViewResId() {
        return R.layout.fragment_home_pager;
    }

    @Override
    protected void initListener() {
        mContentAdapter.setOnListItemClickListener(this);
        mLooperPagerAdapter.setOnLoopPagerItemClickListener(this);
        //???????????????
        homePagerParent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (homeHeaderContainer==null){
                    return;
                }
                int headerHeight = homeHeaderContainer.getMeasuredHeight();
                LogUtils.d(HomePagerFragment.this,"headerHeight-->"+headerHeight);
                homePagerNestedView.setHeaderHeight(headerHeight);
                int measuredHeight = homePagerParent.getMeasuredHeight();
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mContentList.getLayoutParams();
                layoutParams.height = measuredHeight;
                mContentList.setLayoutParams(layoutParams);
                if (measuredHeight!=0) {
                    homePagerParent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        looperPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //??????

            }

            @Override
            public void onPageSelected(int position) {
                //???????????????
                //??????postion???????????????,???????????????
                if (mLooperPagerAdapter.getDataSize()==0) {
                    return;
                }
                int targetPosition = position % mLooperPagerAdapter.getDataSize();
                updateLooperIndicator(targetPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //????????????
            }
        });

        mTwinklingRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                LogUtils.d(HomePagerFragment.this,"??????loadmore...");
                //????????????
                if (mPagerPresenter != null) {
                    mPagerPresenter.loadMore(mMaterialId);
                }
            }

            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                //?????????????????????????????????????????????????????????????????????????????????
                //???????????????????????????????????? ???
                
            }
        });


    }

    /**
     * ???????????????
     *
     * @param targetPosition
     */
    private void updateLooperIndicator(int targetPosition) {
        for (int i = 0; i < looperPointContainer.getChildCount(); i++) {
            View point = looperPointContainer.getChildAt(i);
            if (i == targetPosition) {
                point.setBackgroundResource(R.drawable.shape_indicator_point_selected);
            } else {
                point.setBackgroundResource(R.drawable.shape_indicator_point_noraml);
            }
        }

    }

    @Override
    protected void initView(View rootView) {
        //?????????????????????
        mContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        mContentList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = 8;
                outRect.bottom = 8;
            }
        });
        //???????????????
        mContentAdapter = new LinearItemContentAdapter();
        //???????????????
        mContentList.setAdapter(mContentAdapter);
//       setupState(State.SUCCESS);

        //?????????
        //???????????????
        mLooperPagerAdapter = new LooperPagerAdapter();
        //???????????????
        looperPager.setAdapter(mLooperPagerAdapter);

        //??????refresh????????????
         mTwinklingRefreshLayout.setEnableRefresh(false);
         mTwinklingRefreshLayout.setEnableLoadmore(true);
    }

    @Override
    protected void initPresenter() {
        mPagerPresenter = PresenterManager.getInstance().getCategroyPagerPresenter();
        mPagerPresenter.registerViewCallback(this);
    }

    @Override
    protected void loadData() {
        Bundle arguments = getArguments();//????????????
        String title = arguments.getString(Constants.KEY_HOME_PAGER_TITLE);
        mMaterialId = arguments.getInt(Constants.KEY_HOME_PAGER_MATERIAL_ID);

        LogUtils.d(this, "loadData title-->" + title);
        LogUtils.d(this, "loadData materialId-->" + mMaterialId);
        if (mPagerPresenter != null) {
            mPagerPresenter.getContentByCategoryId(mMaterialId);
        }
        if (currentCategoryTitleTv != null) {
            currentCategoryTitleTv.setText(title);
        }
    }

//    @Override
//    public void getContentByCategoryId(int categoryId) {
//        //????????????id???????????????
//    }

    @Override
    protected void relese() {
        if (mPagerPresenter != null) {
            mPagerPresenter.unregisterViewCallback(this);
        }
    }


    @Override
    public void onContentLoaded(List<HomePagerContent.DataBean> content) {
        //??????????????????
        mContentAdapter.setData(content);

        setupState(State.SUCCESS);
    }

    @Override
    public int getCategoryId() {
        return mMaterialId;
    }

    @Override
    public void onLoading() {
        setupState(State.LOADING);
    }

    @Override
    public void onError() {
        //????????????
        setupState(State.ERROR);
    }

    @Override
    public void onEmpty() {
        setupState(State.EMPTY);
    }

    @Override
    public void onLoaderMoreError() {
        ToastUtils.showToast("????????????,???????????????");
        if (mTwinklingRefreshLayout != null) {
            mTwinklingRefreshLayout.finishLoadmore();
        }
    }

    @Override
    public void onLoaderMoreEmpty() {
        ToastUtils.showToast("?????????????????????");
        if (mTwinklingRefreshLayout != null) {
            //???????????????????????????ui??????????????????
            mTwinklingRefreshLayout.finishLoadmore();
        }
//        Toast.makeText(getContext(),"?????????????????????",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderMoreLoaded(List<HomePagerContent.DataBean> contents) {
        //???????????????????????????
        mContentAdapter.addData(contents);
        if (mTwinklingRefreshLayout != null) {
            mTwinklingRefreshLayout.finishLoadmore();
        }
        ToastUtils.showToast("?????????"+contents.size()+"?????????");
//        Toast.makeText(getContext(),"?????????"+contents.size()+"?????????",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLooperListLoaded(List<HomePagerContent.DataBean> contents) {
        LogUtils.d(this, "looperdata size-->" + contents.size());
        mLooperPagerAdapter.setData(contents);
        //??????????????????
        //?????????%?????????????????????0????????????????????????????????????
        int dx = (Integer.MAX_VALUE / 2) % contents.size();
        int targetCenterPostion = (Integer.MAX_VALUE / 2) - dx;
        looperPager.setCurrentItem(targetCenterPostion);
        LogUtils.d(this, "url-->" + contents.get(0).getPictUrl());

        //?????????
        for (int i = 0; i < contents.size(); i++) {
            View point = new View(getContext());
            //view?????????LinearLayout???
            int size = SizeUtils.dip2px(getContext(), 8);
            //????????????
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
            point.setLayoutParams(layoutParams);
            point.setBackgroundColor(getContext().getColor(R.color.white));
            layoutParams.leftMargin = SizeUtils.dip2px(getContext(), 5);
            layoutParams.rightMargin = SizeUtils.dip2px(getContext(), 5);
            if (i == 0) {
                point.setBackgroundResource(R.drawable.shape_indicator_point_selected);
            } else {
                point.setBackgroundResource(R.drawable.shape_indicator_point_noraml);
            }
            looperPointContainer.addView(point);
        }
    }

    @Override
    public void onItemClick(IBaseInfo item) {
        //?????????????????????
        LogUtils.d(this,"item click -->"+item.getTitle());
        handlerItemClick(item);
    }

    private void handlerItemClick(IBaseInfo item) {
        //?????????????????????????????????????????????
        TicketUtil.toTTicketPage(getContext(),item);
    }

    @Override
    public void onLoopereItemClick(IBaseInfo item) {
        //????????????????????????
        LogUtils.d(this,"loop item click -->"+item.getTitle());
        handlerItemClick(item);
    }
}
