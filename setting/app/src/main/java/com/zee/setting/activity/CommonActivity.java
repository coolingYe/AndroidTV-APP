package com.zee.setting.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.setting.R;
import com.zee.setting.adapter.ScreenNameAdapter;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.views.BaseDialog;

import java.util.ArrayList;
import java.util.List;

public class CommonActivity extends BaseActivity {
   private boolean isOpenSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        DensityUtils.autoWidth(getApplication(), this);
         showScreenDialog();
       // showSoundDialog();
       // showMicrophoneDialog();


        /*麦克风声音**/
      /*  int streamMaxVolume1 = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        Log.i("ssshhh","streamMaxVolume1="+streamMaxVolume1);
        setCallVolume(10);
        int streamVolume1 = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        Log.i("ssshhh","streamVolume1="+streamVolume1);
        for (int i=streamVolume1;i>0;i--){
            setCallVolume(i);
            int streamVolumeaa = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            Log.i("ssshhh","streamVolumeaa="+streamVolumeaa);
        }*/
      //  Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);



    }



    public void showScreenDialog(){
        BaseDialog normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_screen_resolve, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        initScreenList(view);


    }

    private void initScreenList(View view) {
        ConstraintLayout screenSet=view.findViewById(R.id.screen_set);
        Switch manualSetSwitch=view.findViewById(R.id.manual_set_switch);
        RecyclerView listScreenName= view.findViewById(R.id.list_screen_name);
        LinearLayoutManager managerVertical = new LinearLayoutManager(this);
        managerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        listScreenName.setLayoutManager(managerVertical);
        List<String> nameList=new ArrayList<>();
        nameList.add("1080P-60Hz");
        nameList.add("1080P-50Hz");
        nameList.add("720P-60Hz");
        ScreenNameAdapter adapter=new ScreenNameAdapter(nameList);
        adapter.setHasStableIds(true);
        listScreenName.setAdapter(adapter);
        adapter.setItemClick(new ScreenNameAdapter.OnItemClick() {
            @Override
            public void onItemClick(String name) {
                if (isOpenSwitch){
                    adapter.setSelect(name);
                    adapter.notifyDataSetChanged();
                }


            }
        });

        screenSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpenSwitch){
                    isOpenSwitch=true;
                    manualSetSwitch.setChecked(true);
                }else {
                    isOpenSwitch=false;
                    manualSetSwitch.setChecked(false);
                }
            }
        });



    }


    public  void showSoundDialog(){
        BaseDialog normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_key_sound, null);
        normalDialog.setContentView(view);
        normalDialog.show();
    }

    public  void showMicrophoneDialog(){
        BaseDialog normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_microphone, null);
        normalDialog.setContentView(view);
        normalDialog.show();
    }
}
