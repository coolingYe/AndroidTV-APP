package com.zwn.user.data.protocol.response;

import java.util.List;

public class HistoryResp {
    public List<Record> earlier;
    public List<Record> inAWeek;
    public List<Record> thisMonth;
    public List<Record> today;

    public static class Record{
        public String lastPlayTime;
        public int recordId;
        public String skuId;
        public String skuName;
        public String skuUrl;
    }

    public int getTotal(){
        return earlier.size() + inAWeek.size() + thisMonth.size() + today.size();
    }
}
