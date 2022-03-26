package com.program.taobaounion.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.program.taobaounion.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    private State currentState = State.NONE;
    private View mLoadingView;
    private View mSuccessView;
    private View mErrorView;
    private View mEmptyView;

    public enum State{
        NONE,LOADING,SUCCESS,ERROR,EMPTY
    }

    private Unbinder mBind;
    private FrameLayout mBaseContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
          //创建view
        View rootView = inflater.inflate(R.layout.base_fragment_layout, container,false);
        mBaseContainer = rootView.findViewById(R.id.base_container);
        loadStaterView(inflater,container);
        mBind = ButterKnife.bind(this, rootView);
        initView(rootView);
        initPresenter();
        loadData();
        return rootView;
    }

    /**
     * 加载各种状态的view
     * @param inflater
     * @param container
     */
    private void loadStaterView(LayoutInflater inflater, ViewGroup container) {

        //成功的View
        mSuccessView = loadSuccessView(inflater, container);
        mBaseContainer.addView(mSuccessView);

        //Loading的View
        mLoadingView = loadLoadingView(inflater,container);
        mBaseContainer.addView(mLoadingView);

        //错误页面
        mErrorView = loadErrorView(inflater, container);
        mBaseContainer.addView(mErrorView);

        //内容为空的页面
        mEmptyView = loadEmptyView(inflater, container);
        mBaseContainer.addView(mEmptyView);

        setupState(State.NONE);
    }

    protected View loadErrorView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_error,container,false);
    }

    protected View loadEmptyView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_empty,container,false);
    }

    /**
     * 子类通过这个方法来切换状态页面即可
     * @param state
     */
    public void setupState(State state){
        this.currentState=state;

        mSuccessView.setVisibility(currentState== State.SUCCESS?View.VISIBLE:View.GONE);
        mLoadingView.setVisibility(currentState== State.LOADING?View.VISIBLE:View.GONE);

        mErrorView.setVisibility(currentState == State.ERROR? View.VISIBLE: View.GONE);
        mEmptyView.setVisibility(currentState == State.EMPTY? View.VISIBLE: View.GONE);
    }

    /**
     * 加载loading
     * @param inflater
     * @param container
     * @return
     */
    private View loadLoadingView(LayoutInflater inflater, ViewGroup container) {
       return inflater.inflate(R.layout.fragment_loading,container,false);
    }

    protected void initView(View rootView) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBind != null) {
            mBind.unbind();
        }
        relese();
    }

    protected void relese() {
        //释放资源

    }

    protected void initPresenter() {
        //创建Presenter

    }

    protected void loadData() {
        //加载数据

    }

    protected View loadSuccessView(LayoutInflater inflater, ViewGroup container){
        //得到id
        int resId = getRootViewResId();
        //返回view
        return inflater.inflate(resId, container, false);
    }

    protected abstract int getRootViewResId();
}
