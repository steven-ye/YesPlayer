package com.example.yesplayer.ui.smbcifs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.yesplayer.AddServerDialog;
import com.example.yesplayer.Config;
import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;
import com.example.yesplayer.smb.SmbManager;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.SPUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmbFragment extends Fragment {
    String TAG = "SmbFragment";
    SmbViewModel smbViewModel;
    private LoadingDialog loadingDialog;
    private SimpleAdapter adapter;
    private List<Map<String,String>> list = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        smbViewModel = new ViewModelProvider(this).get(SmbViewModel.class);
        View root = inflater.inflate(R.layout.fragment_smbcifs, container, false);

        loadingDialog = new LoadingDialog(getContext(), "扫描中");

        final TextView textView = root.findViewById(R.id.text_list_empty);
        smbViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final ListView listView = root.findViewById(R.id.listview_smbcifs);
        listView.setEmptyView(textView);
        //list = SPUtils.getInstance().getDataList(SERVERLIST);
        list = smbViewModel.getList().getValue();
        adapter = new SimpleAdapter(getContext(), list ,
                R.layout.item_smb_server,
                new String[]{"name","ip"},
                new int[]{R.id.item_server_name, R.id.item_server_ip});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //String name = list.get(position).get("name");
            //String ip = list.get(position).get("ip");
            //getRootFileList(ip); //)getFiles(ip);
            /*
            Intent intent = new Intent(getActivity(), SmbFileActivity.class);
            intent.putExtra("name", list.get(position).get("name"));
            intent.putExtra("ip", list.get(position).get("ip"));
            startActivity(intent);
             */
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putString("title", list.get(position).get("name"));
            args.putString("name", list.get(position).get("name"));
            args.putString("ip", list.get(position).get("ip"));
            navController.navigate(R.id.nav_smbfilelist, args);
        });

        smbViewModel.getList().observe(getViewLifecycleOwner(), maps ->{
            Log.i(TAG,"List size is "+ maps.size());
            Log.i(TAG, new Gson().toJson(maps));
            list.clear();
            list.addAll(maps);
            //listView.deferNotifyDataSetChanged();
            adapter.notifyDataSetChanged();
        });

        smbViewModel.setList(SPUtils.getInstance().getDataList(Config.SP_SERVERS));

        FloatingActionButton fab_refresh = root.findViewById(R.id.fab_smbcifs);
        fab_refresh.setOnClickListener(view -> {
            //Snackbar.make(view, "查找设备", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
            getServers();
        });


        FloatingActionButton fab_add = root.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(view -> {
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
            //showAddForm();
            AddServerDialog dialog = new AddServerDialog();
            dialog.setCancelable(false);
            dialog.setOnClickListener(map -> {
                List<Map<String, String>> newList = new ArrayList<>(list);
                newList.add(map);
                smbViewModel.setList(newList);
            });
            dialog.show(getParentFragmentManager(),"AddServerDialog");
        });
        return root;
    }

    private void getServers(){
        loadingDialog.show();
        IApplication.getExecutor().submit(()->{
            List<Map<String,String>> servers = SmbManager.getInstance().getServerList(getContext());
            getActivity().runOnUiThread(()->{
                loadingDialog.cancel();
                //list.clear();
                //list.addAll(servers);
                smbViewModel.setList(servers);
                //adapter.notifyDataSetChanged();
                SPUtils.getInstance().putDataList(Config.SP_SERVERS,list);
            });
        });
    }
}