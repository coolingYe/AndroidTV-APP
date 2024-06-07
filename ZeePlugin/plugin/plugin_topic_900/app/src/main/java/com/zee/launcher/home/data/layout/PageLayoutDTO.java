package com.zee.launcher.home.data.layout;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class PageLayoutDTO {
    @JSONField(name = "content")
    public List<AppCardListLayout> content = new ArrayList<>();
}
