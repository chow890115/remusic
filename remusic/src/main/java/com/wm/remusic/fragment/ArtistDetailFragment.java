package com.wm.remusic.fragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.info.ArtistInfo;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.R;
import com.wm.remusic.activity.SelectActivity;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.uitl.CommonUtils;

import java.util.ArrayList;

/**
 * Created by wm on 2016/1/7.
 */
public class ArtistDetailFragment extends BaseFragment {
    int currentlyPlayingPosition = 0;
    long artistID = -1;
    LinearLayoutManager layoutManager;
    Toolbar toolbar;
    ActionBar ab;

    private ArrayList<MusicInfo> musicInfos = new ArrayList<>();
    private RecyclerView recyclerView;
    private ArtDetailAdapter artDetailAdapter;

    public static ArtistDetailFragment newInstance(long id) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong("artist_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getLong("artist_id");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recyclerview_detail_item, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        //musicInfos = (ArrayList) MusicUtils.queryMusic(getActivity(), null, artistID + "", IConstants.START_FROM_ARTIST);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        artDetailAdapter = new ArtDetailAdapter(null);
        recyclerView.setAdapter(artDetailAdapter);
        setItemDecoration();
        reloadAdapter();

        ArtistInfo artistInfo = MusicUtils.getArtistinfo(getContext(), artistID);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(getActivity()), 0, 0);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(artistInfo.artist_name);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return view;


    }

    @Override
    public void onPause() {
        super.onPause();

    }

    //设置分割线
    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }


    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                ArrayList<MusicInfo> artistList = (ArrayList) MusicUtils.queryMusic(getActivity(), null, artistID + "", IConstants.START_FROM_ARTIST);
                artDetailAdapter.updateDataSet(artistList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                artDetailAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    class ArtDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        ArrayList<MusicInfo> mList;

        public ArtDetailAdapter(ArrayList<MusicInfo> musicInfos) {
            mList = musicInfos;
            //list.add(0,null);
        }

        //更新adpter的数据
        public void updateDataSet(ArrayList<MusicInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));
            } else {
                return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.musciframent_common_item, viewGroup, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).textView.setText("共" + mList.size() + "首");
                ((CommonItemViewHolder) holder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", mList);
                        getActivity().startActivity(intent);
                    }
                });

            }
            if (holder instanceof ListItemViewHolder) {
                MusicInfo musicInfo = mList.get(position - 1);
                ((ListItemViewHolder) holder).mainTitle.setText(musicInfo.musicName);
                ((ListItemViewHolder) holder).title.setText(musicInfo.artist);
                //判断该条目音乐是否在播放
                if (MusicPlayer.getCurrentAudioId() == musicInfo.songId) {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).playState.setImageResource(R.drawable.song_play_icon);
                } else {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.GONE);
                }

            }
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() + 1 : 0);
        }

        ;


        class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.play_all_number);
                this.select = (ImageView) view.findViewById(R.id.select);
                view.setOnClickListener(this);
            }

            //播放歌手所有歌曲
            @Override
            public void onClick(View v) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[mList.size()];
                        for (int i = 0; i < mList.size(); i++) {
                            list[i] = mList.get(i).songId;
                        }
                        MusicPlayer.playAll(getContext(), list, 0, false);
                    }
                }, 50);
            }


        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            ImageView moreOverflow, playState;
            TextView mainTitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.playState = (ImageView) view.findViewById(R.id.play_state);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                view.setOnClickListener(this);
                //设置弹出菜单
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = new MoreFragment().newInstance(mList.get(getAdapterPosition() - 1).songId + "", IConstants.MUSICOVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });

            }

            //播放歌曲
            @Override
            public void onClick(View v) {
                //// TODO: 2016/1/19
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        long[] list = new long[mList.size()];
                        for (int i = 0; i < mList.size(); i++) {
                            list[i] = mList.get(i).songId;
                        }
                        MusicPlayer.playAll(getContext(), list, getAdapterPosition() - 1, false);
                        Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(currentlyPlayingPosition);
                                notifyItemChanged(getAdapterPosition());
                                currentlyPlayingPosition = getAdapterPosition();
                            }
                        }, 50);
                    }
                }, 100);
            }

        }


    }

}