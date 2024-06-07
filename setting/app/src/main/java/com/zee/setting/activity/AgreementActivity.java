package com.zee.setting.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.base.LoadState;
import com.zee.setting.data.SettingRepository;
import com.zee.setting.data.SettingViewModel;
import com.zee.setting.data.SettingViewModelFactory;
import com.zee.setting.data.protocol.response.AgreementResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AgreementActivity extends BaseActivity {
    private SettingViewModel settingViewModel;
    private TextView agreementTitle;
    private TextView description;
    private int insertNum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        initView();
        initData();
        initObserver();
    }

    private void initView() {
        description = findViewById(R.id.description);
        agreementTitle = findViewById(R.id.agreement_title);
        View btnBack = findViewById(R.id.agreement_back);
        btnBack.setOnClickListener(v -> finish());
    }


    private void initData() {
        SettingViewModelFactory factory = new SettingViewModelFactory(SettingRepository.getInstance());
        settingViewModel = ViewModelProviders.of(this, factory).get(SettingViewModel.class);
        getAgreementInfo();
    }

    private void getAgreementInfo() {
        String agreementCode = getIntent().getStringExtra("agreementCode");

        if (Objects.equals(agreementCode, BaseConstants.ZEE_PRIVACY_AGREEMENT)) {
            agreementTitle.setText(getResources().getString(R.string.agreement_1));
        } else agreementTitle.setText(getResources().getString(R.string.agreement_2));

        //Log.i("ssshhh", "agreementCode="+agreementCode);
        if (!TextUtils.isEmpty(agreementCode)) {
            settingViewModel.getAgreementInfo(agreementCode);
        }

    }

    private void initObserver() {
        settingViewModel.mAgreementState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                AgreementResp agreementResp = settingViewModel.agreementResp;
                if (agreementResp != null) {
                    String content = agreementResp.getContent();
                    //Log.i("ssshhh", "content=" + content);
                    if (!TextUtils.isEmpty(content)) {
                        setHtml(description, content);
                       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Spanned spanned = Html.fromHtml(content, 0, null, new CustomTagHandler(AgreementActivity.this, description.getTextColors()));
                            description.setText(spanned);
                            description.setMovementMethod(LinkMovementMethod.getInstance());
                        }*/
                      /*  Spanned spanned = Html.fromHtml(content);
                        description.setText(spanned);
                        description.setMovementMethod(LinkMovementMethod.getInstance());*/

                    }
                }
            }
        });
    }

    /**
     * 返回指定字符串的所有索引值
     *
     * @param s   原始字符串
     * @param str 指定的字符串
     * @return
     */
    public static List<Integer> getPoint(String s, String str) {
        List<Integer> list = new ArrayList<>();
        int flag = 0;
        while (s.indexOf(str) != -1) {
            String aa = s.substring(0, s.indexOf(str) + str.length());
            flag = flag + aa.length();
            list.add(flag - str.length());
            s = s.substring(s.indexOf(str) + str.length());
        }
        return list;
    }

    /**
     * 设置html文本 - data字符串
     *
     * @param hmtlData
     * @2019.1.1 -支持了下Img适应屏幕宽度
     */
    public void setHtml(TextView textView, String hmtlData) {
        //  || hmtlData.equals("")
        if (null == hmtlData) {
            return;
        }
        String support_data = hmtlData;
        // 先把所有的style干掉 - 正则表达式
        String regEx = " style=\"(.*?)\"";
        // String regEx =  "<style[^>]*?>[\\s\\S]*?<\\/style>";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(support_data);
        if (m.find()) {
            support_data = m.replaceAll("");
        }
        textView.setText(Html.fromHtml(support_data));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
      /*  TextPaint tp = textView.getPaint();
        tp.setFakeBoldText(false); //true表示粗体，false不粗体
        textView.invalidate();*/
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));


    }

}
