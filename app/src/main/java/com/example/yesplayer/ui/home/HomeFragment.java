package com.example.yesplayer.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.yesplayer.AddServerDialog;
import com.example.yesplayer.Config;
import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;
import com.example.yesplayer.SmbFileActivity;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.SPUtils;
import com.example.yesplayer.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xyoye.libsmb.SmbManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private LoadingDialog loadingDialog;
    private SimpleAdapter adapter;
    private List<Map<String,String>> list;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        loadingDialog = new LoadingDialog(getContext(), "扫描中");

        final TextView textView = root.findViewById(R.id.text_list_empty);
        final ListView listView = root.findViewById(R.id.listview_smbcifs);
        listView.setEmptyView(textView);

        list = homeViewModel.getList().getValue();
        adapter = new SimpleAdapter(getContext(), list ,
                R.layout.item_smb_server,
                new String[]{"name","ip"},
                new int[]{R.id.item_server_name, R.id.item_server_ip});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getActivity(), SmbFileActivity.class);
            intent.putExtra("name", list.get(position).get("name"));
            intent.putExtra("ip", list.get(position).get("ip"));
            startActivity(intent);
        });

        homeViewModel.getList().observe(getViewLifecycleOwner(), maps ->{
            //Log.i(TAG,"List size is "+ maps.size());
            //Log.i(TAG, new Gson().toJson(maps));
            list.clear();
            list.addAll(maps);
            //listView.deferNotifyDataSetChanged();
            adapter.notifyDataSetChanged();
            SPUtils.getInstance().putDataList(Config.SP_SERVERS,list);
        });

        homeViewModel.setList(SPUtils.getInstance().getDataList(Config.SP_SERVERS));

        FloatingActionButton fab_refresh = root.findViewById(R.id.fab_smbcifs);
        fab_refresh.setOnClickListener(view -> {
            getServers();
        });

        FloatingActionButton fab_add = root.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(view -> {
            /*
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
             */
            AddServerDialog dialog = new AddServerDialog();
            dialog.setCancelable(false);
            dialog.setOnClickListener(map -> {
                List<Map<String, String>> newList = new ArrayList<>(list);
                newList.add(map);
                homeViewModel.setList(newList);
            });
            dialog.show(getParentFragmentManager(),"AddServerDialog");
        });

        root.findViewById(R.id.local_video).setOnClickListener(this::onClick);
        root.findViewById(R.id.local_audio).setOnClickListener(this::onClick);
        root.findViewById(R.id.local_folder).setOnClickListener(this::onClick);

        return root;
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v){
        NavController navController =Navigation.findNavController(v);
        Bundle args = new Bundle();
        switch(v.getId()){
            case R.id.local_video:
                args.putString("title", getString(R.string.menu_video));
                args.putString("type", "video");
                break;

            case R.id.local_audio:
                args.putString("title", getString(R.string.menu_audio));
                args.putString("type", "audio");
                break;

            default:
                args.putString("title", getString(R.string.menu_phone));
                args.putString("type", "folder");
                break;
        }
        navController.navigate(R.id.nav_filelist, args);
    }

    public void getServers(){
        loadingDialog.show();

        IApplication.getExecutor().submit(()->{
            List<Map<String,String>> servers = SmbManager.getInstance().getServerList();
            getActivity().runOnUiThread(()->{
                loadingDialog.cancel();
                homeViewModel.setList(servers);
            });
        });
    }
}