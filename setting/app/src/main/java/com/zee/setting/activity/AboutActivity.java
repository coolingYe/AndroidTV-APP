package com.zee.setting.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.setting.R;
import com.zee.setting.adapter.DeviceNameAdapter;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.views.BaseDialog;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        DensityUtils.autoWidth(getApplication(), this);
        showDeviceNameDialog();
       // showSetDeviceNameDialog();

    }

    public void showDeviceNameDialog(){
        BaseDialog normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_device_name, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        initDeviceList(view);


    }

    private void initDeviceList(View view) {
        RecyclerView listDeviceName= view.findViewById(R.id.list_device_name);
        LinearLayoutManager managerVertical = new LinearLayoutManager(this);
        managerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        listDeviceName.setLayoutManager(managerVertical);
        List<String> nameList=new ArrayList<>();
        nameList.add("自定义设备名称（AI互动屏盒子）");
        nameList.add("客厅的AI互动屏盒子");
        nameList.add("卧室的AI互动屏盒子");
        nameList.add("书房的AI互动屏盒子");
        nameList.add("办公室的AI互动屏盒子");
        DeviceNameAdapter adapter=new DeviceNameAdapter(nameList);
        adapter.setHasStableIds(true);
        listDeviceName.setAdapter(adapter);
        adapter.setItemClick(new DeviceNameAdapter.OnItemClick() {
            @Override
            public void onItemClick(String name) {
               // Log.i("ssshhh","name="+name);
                adapter.setSelect(name);
                adapter.notifyDataSetChanged();

            }
        });


    }


    public void showSetDeviceNameDialog(){
        BaseDialog normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_set_device_name, null);
        normalDialog.setContentView(view);
        normalDialog.show();
    }

}
