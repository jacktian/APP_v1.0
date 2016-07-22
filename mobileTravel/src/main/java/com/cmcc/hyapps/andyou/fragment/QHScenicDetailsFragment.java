
package com.cmcc.hyapps.andyou.fragment;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.activity.CommentDetailsActivity;
import com.cmcc.hyapps.andyou.activity.CommentEditActivity;
import com.cmcc.hyapps.andyou.activity.DownloadListActivity;
import com.cmcc.hyapps.andyou.activity.GuiderRaidersCommentActivity;
import com.cmcc.hyapps.andyou.activity.IndexActivity;
import com.cmcc.hyapps.andyou.activity.NaviActivity;
import com.cmcc.hyapps.andyou.activity.NavigationDetailActivity;
import com.cmcc.hyapps.andyou.activity.NewPhotoPreviewActivity;
import com.cmcc.hyapps.andyou.activity.QHScenicDetailPhotos;
import com.cmcc.hyapps.andyou.activity.QHScenicDetailPics;
import com.cmcc.hyapps.andyou.activity.QHScenicDetailShops;
import com.cmcc.hyapps.andyou.activity.ScenicLiveActivity;
import com.cmcc.hyapps.andyou.activity.ScenicSpotListActivity;
import com.cmcc.hyapps.andyou.activity.SecnicActivity;
import com.cmcc.hyapps.andyou.activity.ShopsDetailActivity;
import com.cmcc.hyapps.andyou.adapter.ScenicDetailCommentAdapter;
import com.cmcc.hyapps.andyou.adapter.WeatherListAdapter;
import com.cmcc.hyapps.andyou.app.Const;
import com.cmcc.hyapps.andyou.app.MobConst;
import com.cmcc.hyapps.andyou.app.ServerAPI;
import com.cmcc.hyapps.andyou.app.ServerAPI.Comments.Type;
import com.cmcc.hyapps.andyou.app.ServerAPI.ErrorCode;
import com.cmcc.hyapps.andyou.app.ServerAPI.Favorites;
import com.cmcc.hyapps.andyou.app.ServerAPI.ScenicVideos;
import com.cmcc.hyapps.andyou.app.ShareManager;
import com.cmcc.hyapps.andyou.data.DataLoader;
import com.cmcc.hyapps.andyou.data.DataLoader.DataLoaderCallback;
import com.cmcc.hyapps.andyou.data.GsonRequest;
import com.cmcc.hyapps.andyou.data.LocationDetector;
import com.cmcc.hyapps.andyou.data.LocationDetector.LocationListener;
import com.cmcc.hyapps.andyou.data.OfflinePackageManager;
import com.cmcc.hyapps.andyou.data.RequestManager;
import com.cmcc.hyapps.andyou.data.ResponseError;
import com.cmcc.hyapps.andyou.data.UrlListLoader;
import com.cmcc.hyapps.andyou.model.AudioIntro;
import com.cmcc.hyapps.andyou.model.Comment;
import com.cmcc.hyapps.andyou.model.Comment.CommentList;
import com.cmcc.hyapps.andyou.model.Comment.VoteResponse;
import com.cmcc.hyapps.andyou.model.EmptyResponse;
import com.cmcc.hyapps.andyou.model.Location;
import com.cmcc.hyapps.andyou.model.NearbyPeopleCount;
import com.cmcc.hyapps.andyou.model.QHNavigation;
import com.cmcc.hyapps.andyou.model.QHScenic;
import com.cmcc.hyapps.andyou.model.ScenicAudio;
import com.cmcc.hyapps.andyou.model.Weather;
import com.cmcc.hyapps.andyou.util.AnimUtils;
import com.cmcc.hyapps.andyou.util.AppUtils;
import com.cmcc.hyapps.andyou.util.ExcessiveClickBlocker;
import com.cmcc.hyapps.andyou.util.Log;
import com.cmcc.hyapps.andyou.util.ScreenUtils;
import com.cmcc.hyapps.andyou.util.ToastUtils;
import com.cmcc.hyapps.andyou.widget.ActionBar;
import com.cmcc.hyapps.andyou.widget.CommonDialog;
import com.cmcc.hyapps.andyou.widget.CommonDialog.OnDialogViewClickListener;
import com.cmcc.hyapps.andyou.widget.PullToRefreshRecyclerView;
import com.example.rtspdemo.PlayerActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemSubViewClickListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class QHScenicDetailsFragment extends ServiceBaseFragment implements OnClickListener,
        DataLoaderCallback<CommentList> {
    private final String TAG = "ScenicDetailsFragment";

    private final int REQUEST_CODE_LOGIN_NEW_COMMENT = 1;
    private final int REQUEST_CODE_LOGIN_VOTE = 2;
    private final int REQUEST_CODE_LOGIN_COMMENT_DETAIL = 3;
    private final int REQUEST_CODE_SEARCH = 4;
    private final int REQUEST_CODE_COMMENT_DETAIL = 5;
    private final int REQUEST_CODE_POST_COMMENT = 6;
    private static final int ACCELERATION_THRESOLD = 22;

    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private ScenicDetailCommentAdapter mAdapter;

    private  int mIds;
    private  int mId;
    private Location mLocation;
    private View mLoadingProgress;
    private View mReloadView;
    private PullToRefreshRecyclerView mPullToRefreshView;

    private UrlListLoader<CommentList> mCommentLoader;
    private GsonRequest<QHScenic> mDetailsLoadRequest;
    private ViewGroup mRootView;
    private TextView mVoteText;

    private PopupWindow mWeatherDropDown;
    private OfflinePackageManager mOfflineManager;

    private SensorManager mSensorManager;
    private Vibrator mVibrator;
    private boolean mRandomLoad;
    private String secnicNmae;
    private QHScenic scenic;

    private Comment mComment;
    private LocationDetector mLocationDetector;
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onReceivedLocation(Location loc) {
            Log.d("onReceivedLocation, location=%s", loc);
            mLocation = loc;
            //loadScenicDetails();
        }

        @Override
        public void onLocationTimeout() {
            Log.d("onLocationTimeout");
            ((IndexActivity) getActivity()).showLocationSelector();
        }

        @Override
        public void onLocationError() {
            Log.d("onLocationError");
            // TODO
        }
    };

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        private final int UPDATE_INTERVAL = 100;
        private long mLastSensorUpdate = -1;
        private float mLastX, mLastY, mLastZ;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long curTime = System.currentTimeMillis();
                long diffTime = curTime - mLastSensorUpdate;
                if (mLastSensorUpdate == -1 || diffTime > UPDATE_INTERVAL) {
                    mLastSensorUpdate = curTime;
                    float[] values = event.values;
                    if (values == null || values.length < 3) {
                        return;
                    }
                    float x = values[0];
                    float y = values[1];
                    float z = values[2];
                    float deltaX = x - mLastX;
                    float deltaY = y - mLastY;
                    float deltaZ = z - mLastZ;
                    mLastX = x;
                    mLastY = y;
                    mLastZ = z;
                    float totalMovement = Math.max(Math.max(deltaX, deltaY), deltaZ);
                    if (totalMovement > ACCELERATION_THRESOLD
                            && mDetailsLoadRequest != null
                            && mDetailsLoadRequest.hasHadResponseDelivered()) {
                        Log.d("x:%f；y:%f,z:%f", x, y, z);
                        mVibrator.vibrate(new long[] {
                                200, 200, 200, 200
                        }, -1);
                        mRandomLoad = true;
                        reload();
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOfflineManager = OfflinePackageManager.getInstance();
        mLocationDetector = new LocationDetector(getActivity().getApplicationContext());
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        ShareManager.getInstance().onStart(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.activity_list,container,false);
//        secnicNmae  = getArguments().getString(Const.EXTRA_NAME);
        mId = getArguments().getInt(Const.QH_SECNIC_ID);
        //mIds=mId;
        scenic =  getArguments().getParcelable(Const.QH_SECNIC);
        initViews();
        loadScenicDetails();
//        reload();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
      //  reload();//重新加载
        loadScenicDetailsAgain();
        // hookSensorListener(!isHidden());
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
        // hookSensorListener(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mId = getArguments().getInt(Const.EXTRA_ID);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d("onHiddenChanged, hidden=%s", hidden);
        // hookSensorListener(!hidden);
        super.onHiddenChanged(hidden);
    }

    private void hookSensorListener(boolean register) {
        if (register) {
            mSensorManager.registerListener(mSensorEventListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            mSensorManager.unregisterListener(mSensorEventListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

        }
    }

    private void reload() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);

        mAdapter.setHeader(null);
        mAdapter.setDataItems(null);
        mCommentLoader = null;
        mAdapter.notifyDataSetChanged();


        RequestManager.getInstance().getRequestQueue().cancelAll(mRequestTag);


        loadScenicDetails();

//        if (mRandomLoad) {
//            loadScenicDetails();
//            return;
//        }

//        if (mId > 0) {
//            ScenicDetails offlineData = mOfflineManager.getOfflineData(mId, ScenicDetails.class);
//            if (offlineData != null) {
//                Log.d("Using offline package data for scenic %d", mId);
//                onScenicDetailsLoaded(offlineData);
//            } else {
//                loadScenicDetails();
//            }
//        }
//        else {
//            mLocation = getArguments().getParcelable(Const.EXTRA_COORDINATES);
//            if (mLocation != null && mLocation.isValid()) {
//                loadScenicDetails();
//            } else {
//                mLocationDetector.detectLocation(mLocationListener, true, true);
//            }
//        }
    }

    private void initViews() {
        initActionBar();
        initListView();
        initPullToRefresh();

        mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);
    }

    private void initPullToRefresh() {
        mPullToRefreshView = (PullToRefreshRecyclerView)mRootView.findViewById(R.id.pulltorefresh_twowayview);
        mPullToRefreshView.setMode(Mode.DISABLED);
//        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView
//                .findViewById(R.id.pulltorefresh_twowayview);
//        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {
//
//            @Override
//            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
//                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
//                    reload();
//                }
////                else {
////                    loadCommentList();
////                }
//            }
//        });
    }

    private void initListView() {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()
                .getApplicationContext());
//        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ScenicDetailCommentAdapter(getActivity());
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
        clickSupport.setOnItemSubViewClickListener(new OnItemSubViewClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == 0) {
                    onHeaderItemClicked(view);
                } else {
                    onCommentItemClicked(view);
                }
            }
        });
        int scap = ScreenUtils.dpToPxInt(getActivity(), 1);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(mRecyclerView);
        mRecyclerView.addItemDecoration(decor);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    private void initActionBar() {
        mActionBar = (ActionBar) mRootView.findViewById(R.id.action_bar);
        mActionBar.setBackgroundResource(R.color.title_bg);
        mActionBar.getTitleView().setText(secnicNmae);
        mActionBar.getLeftView().setImageResource(R.drawable.return_back);
        mActionBar.getLeftView().setOnClickListener(this);
//        mActionBar.getRightTextView().setVisibility(View.VISIBLE);
//        mActionBar.getRightTextView().setText("导航");
//        mActionBar.getRightTextView().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left:
                getActivity().finish();
                break;
            case R.id.action_bar_right_text: {
                if (scenic!=null){
                    mActionBar.getRightTextView().setVisibility(View.VISIBLE);
                    final QHNavigation tag = new QHNavigation(scenic.longitude,scenic.latitude);
                    //得到经纬度，打开一个dialog
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton("开始导航",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(),NavigationDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("navi_detail",tag);
                            intent.putExtra("navi_bundle",bundle);
                            getActivity().startActivity(intent);
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消导航",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }else {
                    mActionBar.getRightTextView().setVisibility(View.GONE);
                }

//                Intent intent = new Intent(getActivity(), SearchScenicActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SEARCH);
//                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_SEARCH);
                break;
            }
            case R.id.reload_view: {
                reload();
//                loadScenicDetails();
                break;
            }

            default:
                break;
        }
    }

    private void loadScenicDetails() {
        final String url;
//        if (mRandomLoad) {
//            url = ServerAPI.ScenicDetails.buildUrl(-1, null, true);
//        } else {
//            url = ServerAPI.ScenicDetails.buildUrl(mId, mLocation, false);
//        }
        url = ServerAPI.BASE_URL + "scenics/" + mId + "/";
        Log.d("Loading scenic details from %s", url);
        mDetailsLoadRequest = RequestManager.getInstance().sendGsonRequest(Method.GET, url,
                QHScenic.class, null,
                new Response.Listener<QHScenic>() {
                    @Override
                    public void onResponse(QHScenic response) {
                        Log.d("onResponse, ScenicDetails=%s,mAdapter.size=%d", response, mAdapter
                                .getDataItems().size(),mDetailsLoadRequest.getStateCode());
                        mPullToRefreshView.onRefreshComplete();
                        scenic = response;
                        mActionBar.getRightTextView().setVisibility(View.VISIBLE);
                        if (response == null){
                            ToastUtils.show(QHScenicDetailsFragment.this.getActivity(),R.string.delete_information);
                            return;
                        }
//                        mRandomLoad = false;
                        mAdapter.notifyDataSetChanged();
                        onScenicDetailsLoaded(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
//                        if (mScenicDetailsModel == null) {
//                        }
//                        if (mDetailsLoadRequest != null) {
//                            mDetailsLoadRequest.markDelivered();
//                        }
//                        mRandomLoad = false;
//                        if (mDetailsLoadRequest!=null){
//                            if (mDetailsLoadRequest.getStateCode()==0)
//                            ToastUtils.show(getActivity(),R.string.delete_information);
//                        }
                        showReloadView();
                        mPullToRefreshView.onRefreshComplete();
                        }

                }, false, mRequestTag);

      //  android.util.Log.e(TAG,"====="+mDetailsLoadRequest.getStateCode());
    }
    private void loadScenicDetailsAgain() {
        final String url= ServerAPI.BASE_URL + "scenics/" + mId + "/";
        Log.d("Loading scenic details from %s", url);
        mDetailsLoadRequest = RequestManager.getInstance().sendGsonRequest(Method.GET, url,
                QHScenic.class, null,
                new Response.Listener<QHScenic>() {
                    @Override
                    public void onResponse(QHScenic response) {
                        Log.d("onResponse, ScenicDetails=%s,mAdapter.size=%d", response, mAdapter
                                .getDataItems().size());
                        scenic = response;
                        //mRandomLoad = false;
                      //  mAdapter.setDataItems(null);
                        onScenicDetailsLoaded(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }, false, mRequestTag);
    }

    private void showReloadView() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
    }

    private void onScenicDetailsLoaded(QHScenic data) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

//        if (data == null) {
//            showReloadView();
//            return;
//        }

        mScenic = data;

        mId = data.id;
//        mActionBar.getRightTextView().setText(FormatUtils.cutStringStartBy(mScenicDetailsModel.cityZh, 3));

        // Check if we already have an offline archive
//        if (!data.isOfflinePackage) {
//            ScenicDetails offlineData = mOfflineManager
//                    .getOfflineData(data.id, ScenicDetails.class);
//            if (offlineData != null) {
//                mScenicDetailsModel = offlineData;
//                Log.d("Replacing scenic %s with offline data", data.name);
//            }
//        }

        mAdapter.setHeader(data);
        mActionBar.getTitleView().setText(data.name);
//        mAdapter.setDataItems(mScenic.nearby_scenic);
        mAdapter.setDataItems(null);
        mRecyclerView.setAdapter(mAdapter);
        // mActionBar.setTitle(data.name);
        mAdapter.notifyDataSetChanged();
//        loadWeatherInfo();
//        loadCommentList();
        // loadNearbyCounts();
    }


    private void loadCommentList() {
        if (mCommentLoader == null) {
            if(null==getActivity())return;
            mCommentLoader = new UrlListLoader<CommentList>( mRequestTag, CommentList.class);
            mCommentLoader.setUrl(ServerAPI.Comments.buildUrl(getActivity(), mScenic.id, Type.SCENIC));
        }
        mCommentLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }

    @Override
    public void onLoadFinished(CommentList list, int mode) {
//        mPullToRefreshView.onRefreshComplete();
        onCommentListLoaded(list);
    }

    @Override
    public void onLoadError(int mode) {
        Log.e("Error loading scenic details comment for %d", mId);
//        mPullToRefreshView.onRefreshComplete();
    }

    private void onCommentListLoaded(CommentList data) {
        if (data == null || data.list == null
                || (mCommentLoader != null && !mCommentLoader.getPaginator().hasMorePages())) {
            // Disable PULL_FROM_END
//            mPullToRefreshView.setMode(Mode.PULL_FROM_START);
        }

        if (data != null) {
            if (data.pagination != null && data.pagination.offset == 0) {
                mAdapter.setDataItems(null);
            }
//            mAdapter.appendDataItems(data.list);
        }
    }

    private void onHeaderItemClicked(View v) {
        if (mScenic == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.scenic_cover_image: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_BANNER);
                if(scenic.scenic_video.size()!=0){
                    final QHScenic.QHVideo item = scenic.scenic_video.get(0);
                    if(scenic.scenic_video.get(0)!=null){
                        // 0：使用白天视频 1：使用夜间视频
                        Intent intent = null;
                        switch (item.day_or_night){
                            case 0:
                                intent = new Intent(getActivity(),PlayerActivity.class);
                                intent.putExtra("url",item.video_day);
                                getActivity().startActivity(intent);
                                break;
                            case 1:
                                intent = new Intent(getActivity(),PlayerActivity.class);
                                intent.putExtra("url",item.video_night);
                                getActivity().startActivity(intent);
                                break;
                            default:
                                intent = new Intent(getActivity(),PlayerActivity.class);
                                intent.putExtra("url",item.video_day);
                                getActivity().startActivity(intent);

                        }
//                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                        builder.setPositiveButton("白天",new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Intent intent = new Intent(getActivity(),VideoActivity.class);
//                                intent.putExtra("url",item.video_day);
//                                getActivity().startActivity(intent);
//                                dialogInterface.dismiss();
//                            }
//                        });
//                        builder.setNegativeButton("夜间",new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Intent intent = new Intent(getActivity(),NightLiveActivity.class);
//                                intent.putExtra("url",item.video_night);
//                                getActivity().startActivity(intent);
//                                dialogInterface.dismiss();
//                            }
//                        });
//                        builder.show();
                    }
                }else{
                    Toast.makeText(getActivity(),"没有更多视频",Toast.LENGTH_SHORT).show();
                }


//                Intent intent = new Intent(getActivity(),MediaPlayerActivity.class);
//                intent.putExtra("media_url",scenic.scenic_video.get(0).video_night);
//                startActivity(intent);




//                Intent intent = new Intent(getActivity(), ImageGalleryActivity.class);
//                intent.putExtra(Const.EXTRA_ID, mScenic.id);
//                intent.putExtra(Const.EXTRA_NAME, mScenic.name);
//                intent.putExtra(Const.EXTRA_SHOW_POST_IMAGE_BUTTON, true);

                break;
            }
            case R.id.scenic_wish_to_go: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_WISH);
                processWishlist((TextView) v, mScenicDetailsModel.isFavorite);
                break;
            }
//            case R.id.scenic_detail_func_listen: {
//                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_LISTEN);
//                if (!mScenicDetailsModel.hasAudioResources()) {
//                    Toast.makeText(getActivity(), R.string.audio_resource_unavailable, Toast.LENGTH_SHORT).show();
//                    // return;
//                }
//
//                ArrayList<ScenicAudio> audioIntro = new ArrayList<ScenicAudio>(
//                        mScenicDetailsModel.audioIntroSections.size());
//                for (ScenicAudio audio : mScenicDetailsModel.audioIntroSections) {
//                    if (audio.audio != null) {
//                        // TODO:
//                        Iterator<AudioIntro> it = audio.audio.iterator();
//                        while (it.hasNext()) {
//                            // if
//                            // (it.next().title.equals(getString(R.string.audio_title_intro)))
//                            // {
//                            // it.remove();
//                            // }
//                            AudioIntro a = it.next();
//                            a.scenicImage = audio.image;
//                            a.scenicName = audio.spotName;
//                        }
//                        // audio.validate();
//                        audioIntro.add(audio);
//                    }
////                    audioIntro.add(audio);
//                }
//
//                Intent intent = new Intent(getActivity().getApplicationContext(), ListenActivity.class);
//                intent.putParcelableArrayListExtra(Const.EXTRA_AUDIO, audioIntro);
//                intent.putExtra(Const.EXTRA_ID, mScenicDetailsModel.id);
//                intent.putExtra(Const.EXTRA_NAME, mScenicDetailsModel.name);
//                startActivity(intent);
//                break;
//            }
//            case R.id.scenic_detail_download: {
//                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_DOWNLOAD);
//                if (mScenicDetailsModel.offlinePackage != null
//                        && mScenicDetailsModel.offlinePackage.isValid()
//                        && mOfflineManager.getDownloadStatus(mScenicDetailsModel.id) == -1) {
//                    showDownloadDialog();
//                } else {
//                    Intent intent = new Intent(getActivity(),
//                            DownloadListActivity.class);
//                    getActivity().startActivity(intent);
//                }
//                break;
//            }
            case R.id.scenic_detail_func_navi: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_NAVI);
                if (mScenicDetailsModel.location != null && mScenicDetailsModel.location.isValid()) {
                    Intent intent = new Intent(getActivity(), NaviActivity.class);
                    intent.putExtra(Const.EXTRA_SCENIC_DATA, mScenicDetailsModel);
                    intent.putExtra(Const.EXTRA_ID, mScenicDetailsModel.id);
                    mScenicDetailsModel.location.city = mScenicDetailsModel.cityZh;
                    intent.putExtra(Const.EXTRA_COORDINATES, mScenicDetailsModel.location);



                    if (!mScenicDetailsModel.hasAudioResources()) {
                        Toast.makeText(getActivity(), R.string.audio_resource_unavailable, Toast.LENGTH_SHORT).show(); }
                    ArrayList<ScenicAudio> audioIntro = new ArrayList<ScenicAudio>( mScenicDetailsModel.audioIntroSections.size());
                    for (ScenicAudio audio : mScenicDetailsModel.audioIntroSections) {
                        if (audio.audio != null) {
                            Iterator<AudioIntro> it = audio.audio.iterator();
                            while (it.hasNext()) {
                                AudioIntro a = it.next();
                                a.scenicImage = audio.image;
                                a.scenicName = audio.spotName;
                            }
                            audioIntro.add(audio);
                        }
//                        audioIntro.add(audio);
                    }
                    intent.putParcelableArrayListExtra(Const.EXTRA_AUDIO, audioIntro);
                    intent.putExtra(Const.EXTRA_ID, mScenicDetailsModel.id);
                    intent.putExtra(Const.EXTRA_NAME, mScenicDetailsModel.name);



                    startActivity(intent);
                }
                break;
            }
            case R.id.scenic_detail_func_live: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_LIVE);
                Intent intent = new Intent(getActivity(), ScenicLiveActivity.class);
                intent.putExtra(Const.EXTRA_SCENIC_DATA, mScenicDetailsModel);
                intent.putExtra(Const.EXTRA_ID, mScenicDetailsModel.id);
                intent.putExtra(Const.EXTRA_VIDEO_TYPE, ScenicVideos.Type.SCENIC);
                intent.putExtra(Const.EXTRA_NAME, mScenicDetailsModel.name);
                startActivity(intent);
                break;
            }

            case R.id.scenic_detail_func_service: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_SERVICE);
                showServicePopup();
                break;
            }

            case R.id.scenic_intro_container: {
                MobclickAgent.onEvent(getActivity(),MobConst.ID_INDEX_SCENIC_SPOTS);
                Intent intent = new Intent(getActivity(), ScenicSpotListActivity.class);
                intent.putExtra(Const.EXTRA_SCENIC_DATA, mScenicDetailsModel);
                intent.putExtra(Const.EXTRA_ID, mScenicDetailsModel.id);
                intent.putExtra(Const.EXTRA_NAME, mScenicDetailsModel.name);
                startActivity(intent);
                break;
            }

//            case R.id.scenic_new_comment: {
//                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_COMMENT_EDIT);
//                if (!UserManager.makeSureLogin(getActivity(), REQUEST_CODE_LOGIN_NEW_COMMENT)) {
//                    postNewComment();
//                }
//
//                break;
//            }

            case R.id.weather_container: {
                showWeatherDropDown();
                break;
            }

            case R.id.comment_total:{//评论
//                Intent intent = new Intent(getActivity(),TotalComment.class);
                Intent intent = new Intent(getActivity(),GuiderRaidersCommentActivity.class);
                intent.putExtra(Const.QH_SECNIC,scenic);
                intent.putExtra(Const.QH_SECNIC_ID,mId);
                startActivity(intent);
                break;
            }
            case R.id.pics_total:{// 图集
//                Intent intent = new Intent(getActivity(),TotalComment.class);
                Intent intent = new Intent(getActivity(),QHScenicDetailPhotos.class);

                intent.putExtra(Const.QH_SECNIC,scenic);
                intent.putExtra(Const.QH_SECNIC_ID, mId);
                startActivity(intent);
                break;
            }
            case R.id.shop_total:{// shop
//                Intent intent = new Intent(getActivity(),TotalComment.class);
                Intent intent = new Intent(getActivity(),QHScenicDetailShops.class);

                intent.putExtra(Const.QH_SECNIC,scenic);
                intent.putExtra(Const.QH_SECNIC_ID, mId);
                startActivity(intent);
                break;
            }
            case R.id.scenci_total:{// 图集
//                Intent intent = new Intent(getActivity(),TotalComment.class);
                Intent intent = new Intent(getActivity(),QHScenicDetailPics.class);

                intent.putExtra(Const.QH_SECNIC,scenic);
                intent.putExtra(Const.QH_SECNIC_ID, mId);
                startActivity(intent);
                break;
            }
            case R.id.scenery_detail_buttom_photo1://
                jumpPhotosPreview(0);
                break;

            case R.id.scenery_detail_buttom_photo2://
                jumpPhotosPreview(1);
                break;

            case R.id.scenery_detail_buttom_photo3:
                jumpPhotosPreview(2);
                break;

            case R.id.scenery_detail_buttom_shop1:
                jumpShopsPreview(0);
                break;

            case R.id.scenery_detail_buttom_shop2:
                jumpShopsPreview(1);
                break;

            case R.id.scenery_detail_buttom_shop3:
                jumpShopsPreview(2);
                break;

            case R.id.scenery_detail_buttom_pic1:
                jumpPicsPreview(0);
                break;

            case R.id.scenery_detail_buttom_pic2:
                jumpPicsPreview(1);
                break;

            case R.id.scenery_detail_buttom_pic3:
                jumpPicsPreview(2);
                break;

            default:
                break;
        }
    }

    private void jumpPhotosPreview(int position) {
        ArrayList<String> urllist=new ArrayList<String>();
        for (int i = 0; i <mScenic.photos.size() ; i++) {
            if (mScenic.photos.get(i)!=null){
                urllist.add(mScenic.photos.get(i).image_url);
            }
        }
        Intent intent = new Intent(getActivity(),NewPhotoPreviewActivity.class);
//                    intent.putExtra(Const.EXTRA_IMAGE_DATA,  urllist);
        intent.putStringArrayListExtra(Const.EXTRA_IMAGE_DATA, urllist);
        intent.putExtra(Const.EXTRA_IMAGE_PREVIEW_START_INDEX, position);
        startActivity(intent);
    }

    private void jumpShopsPreview(int position) {
        Intent intent = new Intent(getActivity(), ShopsDetailActivity.class);
        int mId = mScenic.nearby_shop.get(position).id;
        intent.putExtra(Const.QH_SECNIC_ID, mId);
        startActivity(intent);
    }
    private void jumpPicsPreview(int position) {
        Intent intent = new Intent(getActivity(), SecnicActivity.class);
        int mId = mScenic.nearby_scenic.get(position).id;
        intent.putExtra(Const.QH_SECNIC, mScenic);
        intent.putExtra(Const.QH_SECNIC_ID,mId);
        startActivity(intent);
    }


    private void showWeatherDropDown() {
        if (mScenicDetailsModel.weather == null || mScenicDetailsModel.weather.weatherDays == null
                || mScenicDetailsModel.weather.weatherDays.isEmpty()) {
            return;
        }
        View contentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.layout_weather_list, mRootView, false);
        RecyclerView recyclerView = (RecyclerView) contentView
                .findViewById(R.id.weather_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        WeatherListAdapter weatherAdapter = new WeatherListAdapter(
                mScenicDetailsModel.weather.weatherDays);
        recyclerView.setAdapter(weatherAdapter);
        mWeatherDropDown = new PopupWindow(contentView, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        mWeatherDropDown.setOutsideTouchable(true);
        mWeatherDropDown.setFocusable(true);
        contentView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mWeatherDropDown.dismiss();
                return true;
            }
        });
        mWeatherDropDown.showAsDropDown(mRootView.findViewById(R.id.action_bar));
    }

    private void showDownloadDialog() {
        CommonDialog downloadDialog = new CommonDialog(getActivity());
        downloadDialog.setTitleText(R.string.offline_download);
        downloadDialog.getDialog().setCancelable(true);
        downloadDialog.getDialog().setCanceledOnTouchOutside(true);
        downloadDialog.setContentText(getString(R.string.offline_download_prompt,
                mScenicDetailsModel.name,
                Formatter.formatFileSize(getActivity(), mScenicDetailsModel.offlinePackage.size)));
        downloadDialog.setOnDialogViewClickListener(new OnDialogViewClickListener() {

            @Override
            public void onRightButtonClick() {
            }

            @Override
            public void onLeftButtonClick() {
                mOfflineManager.downloadPackage(mScenicDetailsModel);
                Intent intent = new Intent(getActivity(),
                        DownloadListActivity.class);
                getActivity().startActivity(intent);
            }
        });
        downloadDialog.showDialog();
    }

    private void postNewComment() {
        Intent commentEdit = new Intent(getActivity(), CommentEditActivity.class);
        commentEdit.putExtra(Const.EXTRA_ID, mScenic.id);
        startActivityForResult(commentEdit, REQUEST_CODE_POST_COMMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN_NEW_COMMENT:
                    postNewComment();
                    break;
                case REQUEST_CODE_LOGIN_VOTE:
                    voteComment();
                    break;
                case REQUEST_CODE_LOGIN_COMMENT_DETAIL:
                    jumpCommentDetail();
                    break;
                case REQUEST_CODE_SEARCH:
                    int id = data.getIntExtra(Const.EXTRA_ID, -1);
                    if (id > -1 && mId != id) {
                        mId = id;
                        reload();
                    } else {
                        Log.e("error onActivityResult, id = ", id);
                    }
                    break;
                case REQUEST_CODE_COMMENT_DETAIL:
                    Comment comment = (Comment) data.getParcelableExtra(Const.EXTRA_COMMENT_DATA);
                    if (comment != null) {
                        mComment.commentCount = comment.commentCount;
                        mComment.isVoted = comment.isVoted;
                        mComment.voteCount = comment.voteCount;
//                        List<Comment> dataItems = mAdapter.getDataItems();
//                        if (dataItems != null) {
//                            int position = -1;
//                            for (int i = 0; i < dataItems.size(); i++) {
//                                if (dataItems.get(i).id == mComment.id) {
//                                    position = i;
//                                    break;
//                                }
//                            }

//                            if (position >= 0) {
//                                // Count the header item
//                                position++;
//                                mAdapter.notifyItemChanged(position);
//                            }
                        }

//                    break;
//                case REQUEST_CODE_POST_COMMENT: {
//                    Comment c = (Comment) data.getParcelableExtra(Const.EXTRA_COMMENT_DATA);
//                    if (c != null) {
//                        mAdapter.getDataItems().add(0, c);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                    break;
//                }
                default:
                    break;
            }
        }
    }

    private void onCommentItemClicked(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        mComment = (Comment) v.getTag();
        if (mComment == null) {
            Log.e("NULL comment");
            return;
        }

        switch (v.getId()) {
            case R.id.item_comment_root:
            case R.id.comment_cover_image:
            case R.id.comment_count: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_COMMENT_DETAIL);
                AnimUtils.doScaleFadeAnim(v);
                // if (!UserManager.makeSureLogin(getActivity(),
                // REQUEST_CODE_LOGIN_COMMENT_DETAIL)) {
                jumpCommentDetail();
                // }
                break;
            }

            case R.id.comment_vote_count: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_COMMENT_VOTE);
                AnimUtils.doScaleFadeAnim(v);
                mVoteText = (TextView) v;
                // if (!UserManager.makeSureLogin(getActivity(),
                // REQUEST_CODE_LOGIN_VOTE)) {
                voteComment();
                // }
                break;
            }

            case R.id.comment_share: {
                ShareManager.getInstance().shareComment(mComment);
                break;
            }

            default:
                break;
        }
    }

    private void jumpCommentDetail() {
        if (mComment == null) {
            Log.e("[jumpCommentDetail] NULL comment");
            return;
        }
        Intent intent = new Intent(getActivity(), CommentDetailsActivity.class);
        intent.putExtra(Const.EXTRA_COMMENT_DATA, mComment);
        startActivityForResult(intent, REQUEST_CODE_COMMENT_DETAIL);
    }

    private void voteComment() {
        if (mComment == null || mVoteText == null) {
            Log.e("[voteComment] NULL comment:" + mComment);
            return;
        }
        Map<String, String> params = ServerAPI.Comments.buildVoteParams(getActivity(), mComment.id,
                Type.COMMENT);
        RequestManager.getInstance().sendGsonRequest(ServerAPI.Comments.VOTE_URL,
                VoteResponse.class,
                new Response.Listener<VoteResponse>() {
                    @Override
                    public void onResponse(VoteResponse response) {
                        mVoteText.setText(String.valueOf(response.voteCount));
                        mVoteText.getCompoundDrawables()[0].setLevel(2);
                        mComment.isVoted = true;
                        mComment.voteCount = response.voteCount;
                        mVoteText.setEnabled(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error voteComment: %s", error);
                        if (error instanceof ResponseError
                                && ((ResponseError) error).errCode == ErrorCode.ERROR_ALREADY_VOTED) {
                            Toast.makeText(getActivity(), R.string.error_already_voted,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            AppUtils.handleResponseError(getActivity(), error);
                        }
                    }
                }, false, params, mRequestTag);
    }

    private void processWishlist(final TextView wishText, final boolean remove) {
        wishText.setEnabled(false);

        Map<String, String> params = Favorites.buildAddFavoritesParams(
                mScenicDetailsModel.id,
                Favorites.Type.SCENIC);
        String url = remove ? Favorites.FAVOR_DELETE_URL
                : Favorites.FAVOR_ADD_URL;
        RequestManager.getInstance().sendGsonRequest(url,
                EmptyResponse.class,
                new Response.Listener<EmptyResponse>() {
                    @Override
                    public void onResponse(EmptyResponse response) {
                        mScenicDetailsModel.isFavorite = !remove;
                        Drawable drawable;
                        if (mScenicDetailsModel.isFavorite) {
                            drawable = getResources().getDrawable(
                                    R.drawable.wish_to_go_selected);
                        } else {
                            drawable = getResources().getDrawable(
                                    R.drawable.wish_to_go);
                        }
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                                drawable.getMinimumHeight());
                        wishText.setCompoundDrawables(drawable, null, null, null);

                        wishText.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error addToWishlist: %s", error);
                        AppUtils.handleResponseError(getActivity(), error);
                        wishText.setEnabled(true);
                    }
                }, false, params, mRequestTag);
    }

    private void loadWeatherInfo() {
        final String url = ServerAPI.Weather.buildUrl(mScenicDetailsModel.city);
        Log.d("Loading weather from %s", url);

        RequestManager.getInstance().sendGsonRequest(url, Weather.class,
                new Response.Listener<Weather>() {
                    @Override
                    public void onResponse(Weather response) {
                        Log.d("onResponse, Weather=%s", response);
                        if (response == null) {
                            return;
                        }

                        mScenicDetailsModel.weather = response;
                        mAdapter.notifyItemChanged(0);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading weather from %s", url);
                    }
                }, mRequestTag);
    }

    private void loadNearbyCounts() {
        final String url = ServerAPI.ScenicDetails.buildNearbyPeopleUrl(mScenicDetailsModel.id);
        Log.d("loadNearbyCounts from %s", url);

        RequestManager.getInstance().sendGsonRequest(url, NearbyPeopleCount.class,
                new Response.Listener<NearbyPeopleCount>() {
                    @Override
                    public void onResponse(NearbyPeopleCount response) {
                        Log.d("onResponse, NearbyPeopleCount=%s", response);
                        if (response == null) {
                            return;
                        }

                        mScenicDetailsModel.nearbyPeopleCount = response;
                        mAdapter.notifyItemChanged(0);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading nearby count from %s", url);
                    }
                }, mRequestTag);
    }

    @Override
    public void onDestroy() {
        if (mLocationDetector != null) {
            mLocationDetector.close();
        }
        ShareManager.getInstance().onEnd();
        super.onDestroy();
    }
}