package com.program.taobaounion.ui.fragment;

import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.program.taobaounion.R;
import com.program.taobaounion.base.BaseFragment;
import com.program.taobaounion.model.domain.Histories;
import com.program.taobaounion.model.domain.IBaseInfo;
import com.program.taobaounion.model.domain.SearchRecommend;
import com.program.taobaounion.model.domain.SearchResult;
import com.program.taobaounion.presenter.ISearchPresenter;
import com.program.taobaounion.ui.adapter.LinearItemContentAdapter;
import com.program.taobaounion.ui.custom.TextFlowLayout;
import com.program.taobaounion.utils.KeyboardUtil;
import com.program.taobaounion.utils.LogUtils;
import com.program.taobaounion.utils.PresenterManager;
import com.program.taobaounion.utils.SizeUtils;
import com.program.taobaounion.utils.TicketUtil;
import com.program.taobaounion.utils.ToastUtils;
import com.program.taobaounion.view.ISearchPageCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class SearchFragment extends BaseFragment implements ISearchPageCallback, TextFlowLayout.OnFlowItemClickListener {

    @BindView(R.id.search_history_view)
    public TextFlowLayout mHistoriesView;

    @BindView(R.id.search_recommend_view)
    public TextFlowLayout mRecommendView;

    @BindView(R.id.search_recommend_container)
    public View mRecommendContainer;

    @BindView(R.id.search_histories_container)
    public View mHistoriesContainer;

    @BindView(R.id.search_history_delete)
    public View mHistoriesDelete;

    @BindView(R.id.search_result_list)
    public RecyclerView mSearchList;

    @BindView(R.id.search_btn)
    public TextView mSearchBtn;

    @BindView(R.id.search_clean_btn)
    public ImageView mCleanInputBtn;

    @BindView(R.id.search_input_box)
    public EditText mSearchInputBox;


    @BindView(R.id.search_result_container)
    public TwinklingRefreshLayout mRefreshContainer;

    private ISearchPresenter mSearchPresenter;
    private LinearItemContentAdapter mSearchResultAdapter;

    @Override
    protected void initPresenter() {
        mSearchPresenter = PresenterManager.getInstance().getSearchPresenter();
        mSearchPresenter.registerViewCallback(this);
        //?????????????????????
        mSearchPresenter.getRecommendWords();
//        mSearchPresenter.doSearch("??????");
        mSearchPresenter.getHistories();
    }

    @Override
    protected void relese() {
        if (mSearchPresenter != null) {
            mSearchPresenter.unregisterViewCallback(this);
        }
    }

    @Override
    protected void onRetryClick() {
        //????????????
        if (mSearchPresenter != null) {
            mSearchPresenter.research();
        }
    }

    @Override
    protected View loadRootView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_search_layout,container,false);
    }

    @Override
    protected int getRootViewResId() {
        return R.layout.fragment_search;
    }

    /**
     * ??????????????????????????????
     * @param containSpace  true???????????????????????????false?????????????????????
     * @return
     */
    private boolean hasInput(boolean containSpace){
        if (containSpace){
            return mSearchInputBox.getText().toString().length()>0;
        }else {
            return mSearchInputBox.getText().toString().trim().length()>0;
        }
    }

    @Override
    protected void initListener() {
        mHistoriesView.setOnFlowItemClickListener(this);
        mRecommendView.setOnFlowItemClickListener(this);
        //????????????
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????
                //??????????????????
                if (hasInput(false)) {
                    //????????????
                    if (mSearchPresenter != null) {
                        toSearch(mSearchInputBox.getText().toString().trim());
//                        mSearchPresenter.doSearch(mSearchInputBox.getText().toString().trim());
                        KeyboardUtil.hide(getContext(),v);
                    }
                }else{
                    //????????????
                    KeyboardUtil.hide(getContext(),v);
                }
            }
        });
        //???????????????????????????
        mCleanInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInputBox.setText("");
                //????????????????????????
                switch2HistoryPage();
            }
        });
        //??????????????????????????????
        mSearchInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //???????????????
                LogUtils.d(SearchFragment.this,"input text ==> " + s.toString().trim());
                //????????????????????????????????????????????????
                //????????????????????????
                mCleanInputBtn.setVisibility(hasInput(true)?View.VISIBLE:View.GONE);
                mSearchBtn.setText(hasInput(false)?"??????":"??????");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchInputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                LogUtils.d(SearchFragment.this,"actionid ==>"+actionId);
                //??????????????????????????????????????????
                if (actionId == EditorInfo.IME_ACTION_SEARCH&&mSearchPresenter!=null){
                    String keyword = v.getText().toString().trim();
                    //?????????????????????????????????
                    if (TextUtils.isEmpty(keyword)) {
                        return false;
                    }
                    LogUtils.d(SearchFragment.this,"input text ===>"+keyword);
                    //????????????
                    toSearch(keyword);
//                    mSearchPresenter.doSearch(keyword);
                }
                return false;
            }
        });
        mHistoriesDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????
                mSearchPresenter.delHistories();
            }
        });
        mRefreshContainer.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                //?????????????????????
                if (mSearchPresenter != null) {
                    mSearchPresenter.loaderMore();
                }
            }
        });
        mSearchResultAdapter.setOnListItemClickListener(new LinearItemContentAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(IBaseInfo item) {
                //???????????????????????????
                TicketUtil.toTTicketPage(getContext(),item);
            }
        });
    }

    /**
     *??????????????????????????????
     */
    private void switch2HistoryPage() {
        if (mSearchPresenter != null) {
            mSearchPresenter.getHistories();
        }
//        if (mHistoriesView.getContentSize()!=0) {
//            mHistoriesContainer.setVisibility(View.VISIBLE);
//        }else {
//            mHistoriesContainer.setVisibility(View.GONE);
//        }
        if (mRecommendView.getContentSize() != 0) {
            mRecommendContainer.setVisibility(View.VISIBLE);
        }else {
            mRecommendContainer.setVisibility(View.GONE);
        }
        //???????????????
        mRefreshContainer.setVisibility(View.GONE);
    }

    protected void initView(View rootView) {
        setupState(State.SUCCESS);
        //?????????????????????
        mSearchList.setLayoutManager(new LinearLayoutManager(getContext()));
        //???????????????
        mSearchResultAdapter = new LinearItemContentAdapter();
        mSearchList.setAdapter(mSearchResultAdapter);
        //??????????????????
        mRefreshContainer.setEnableLoadmore(true);
        mRefreshContainer.setEnableRefresh(false);
        mRefreshContainer.setEnableOverScroll(true);
        mSearchList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top= SizeUtils.dip2px(getContext(),1.5f);
                outRect.bottom=SizeUtils.dip2px(getContext(),1.5f);
            }
        });
    }


    @Override
    public void onHistoriesLoaded(Histories histories) {
        LogUtils.d(this,"histories -->" + histories);
        if (histories  ==null || histories.getHistories().size()==0){
            mHistoriesContainer.setVisibility(View.GONE);
        }else {
            mHistoriesContainer.setVisibility(View.VISIBLE);
            mHistoriesView.setTextList(histories.getHistories());
        }
    }

    @Override
    public void onHistoriesDeleted() {
        //??????????????????
        if (mSearchPresenter != null) {
            mSearchPresenter.getHistories();
        }
    }

    @Override
    public void onSearchSuccess(SearchResult result) {
        setupState(State.SUCCESS);
//        LogUtils.d(this,"result-->"+result.getData());
        //??????????????????????????????
        mRecommendContainer.setVisibility(View.GONE);
        mHistoriesContainer.setVisibility(View.GONE);
        //??????????????????
        mRefreshContainer.setVisibility(View.VISIBLE);
        //????????????
        try {
            mSearchResultAdapter.setData(result.getData().getTbkDgMaterialOptionalResponse().getResultList().getMapData());
        }catch (Exception e){
            e.printStackTrace();
            //???????????????????????????
            setupState(State.EMPTY);
        }
    }

    @Override
    public void onMoreLoaded(SearchResult result) {
        //??????????????????
        //??????????????????????????????????????????
        List<SearchResult.DataBean.TbkDgMaterialOptionalResponseBean.ResultListBean.MapDataBean> moreData = result.getData().getTbkDgMaterialOptionalResponse().getResultList().getMapData();
        mSearchResultAdapter.addData(moreData);
        //??????????????????????????????
        ToastUtils.showToast("????????????"+moreData.size()+"?????????");
        mRefreshContainer.finishLoadmore();
    }

    @Override
    public void onMoreLoadedError() {
        mRefreshContainer.finishLoadmore();
        ToastUtils.showToast("??????????????????????????????");
    }

    @Override
    public void onMoreLoadedEmpty() {
        mRefreshContainer.finishLoadmore();
        ToastUtils.showToast("??????????????????");
    }

    @Override
    public void onRecommendWordsLoaded(List<SearchRecommend.DataBean> recommendWords) {
        LogUtils.d(this,"recommendWords -->"+recommendWords);
        List<String> recommendKeywords = new ArrayList<>();
        for (SearchRecommend.DataBean item : recommendWords) {
            recommendKeywords.add(item.getKeyword());
        }
        LogUtils.d(this,"mText -->"+recommendKeywords);
        if (recommendKeywords==null||recommendKeywords.size()==0){
            mRecommendContainer.setVisibility(View.GONE);
        }else {
            mRecommendView.setTextList(recommendKeywords);
            mRecommendContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onError() {
    setupState(State.ERROR);
    }

    @Override
    public void onLoading() {
    setupState(State.LOADING);
    }

    @Override
    public void onEmpty() {
setupState(State.EMPTY);
    }

    @Override
    public void onFlowItemClick(String text) {
        //????????????
        toSearch(text);
    }

    private void toSearch(String text) {
        if (mSearchPresenter != null) {
            mSearchList.scrollToPosition(0);
            mSearchInputBox.setText(text);
            mSearchInputBox.setFocusable(true);
            mSearchInputBox.requestFocus();
            mSearchInputBox.setSelection(text.length(),text.length());
            mSearchPresenter.doSearch(text);
        }
    }
}
