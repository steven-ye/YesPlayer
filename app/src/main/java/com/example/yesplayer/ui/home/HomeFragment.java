package com.example.yesplayer.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.yesplayer.Config;
import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;
import com.example.yesplayer.smb.SmbManager;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.SPUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private LoadingDialog loadingDialog;
    private SimpleAdapter adapter;
    private List<Map<String,String>> list;
    private File[] fileList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        loadingDialog = new LoadingDialog(getContext(), "扫描中, 请稍等");

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
            loadingDialog.show();
            /*
            Intent intent = new Intent(getActivity(), SmbFileActivity.class);
            intent.putExtra("name", list.get(position).get("name"));
            intent.putExtra("ip", list.get(position).get("ip"));
            startActivity(intent);
            */
            NavController navController =Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putString("title", list.get(position).get("name"));
            args.putString("name", list.get(position).get("name"));
            args.putString("ip", list.get(position).get("ip"));
            navController.navigate(R.id.nav_smbfilelist, args);
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

        /*
        final EmptyRecyclerView recyclerView = root.findViewById(R.id.file_list);
        View emptyView = root.findViewById(R.id.file_list_empty);
        recyclerView.setEmptyView(emptyView);
        fileList = homeViewModel.getFileList().getValue();
        FileAdapter fileAdapter = new FileAdapter(R.layout.item_file,fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(fileAdapter);
        */

        root.findViewById(R.id.local_video).setOnClickListener(this::onClick);
        root.findViewById(R.id.local_audio).setOnClickListener(this::onClick);
        root.findViewById(R.id.local_folder).setOnClickListener(this::onClick);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.item_refresh){
            getServers();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                args.putString("type", "");
                break;
        }
        navController.navigate(R.id.nav_filelist, args);
    }

    public void getServers(){
        loadingDialog.show();

        IApplication.getExecutor().submit(()->{
            List<Map<String,String>> servers = SmbManager.getInstance().getServerList(getContext());
            getActivity().runOnUiThread(()->{
                loadingDialog.cancel();
                homeViewModel.setList(servers);
            });
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(loadingDialog.isShowing()) loadingDialog.dismiss();
    }
}