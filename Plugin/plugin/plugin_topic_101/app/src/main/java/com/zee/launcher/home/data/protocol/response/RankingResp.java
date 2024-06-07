package com.zee.launcher.home.data.protocol.response;

import java.util.List;

public class RankingResp {

    private List<RankingTopN> rankingTopN;
    private Self self;

    public List<RankingTopN> getRankingTopN() {
        return rankingTopN;
    }

    public void setRankingTopN(List<RankingTopN> rankingTopN) {
        this.rankingTopN = rankingTopN;
    }

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }

    public static class RankingTopN {

        private int activityId;
        private String pic;
        private int playScore;
        private String rankingGetTime;
        private int rankingNum;
        private String userId;
        private String userName;

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public RankingTopN(int activityId, String pic, int playScore, String rankingGetTime, int rankingNum, String userId, String userName) {
            this.activityId = activityId;
            this.pic = pic;
            this.playScore = playScore;
            this.rankingGetTime = rankingGetTime;
            this.rankingNum = rankingNum;
            this.userId = userId;
            this.userName = userName;
        }

        public void setActivityId(int activityId) {
            this.activityId = activityId;
        }

        public int getActivityId() {
            return activityId;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }

        public String getPic() {
            return pic;
        }

        public void setPlayScore(int playScore) {
            this.playScore = playScore;
        }

        public int getPlayScore() {
            return playScore;
        }

        public void setRankingGetTime(String rankingGetTime) {
            this.rankingGetTime = rankingGetTime;
        }

        public String getRankingGetTime() {
            return rankingGetTime;
        }

        public void setRankingNum(int rankingNum) {
            this.rankingNum = rankingNum;
        }

        public int getRankingNum() {
            return rankingNum;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

    }

    public static class Self {

        private int activityId;
        private String pic;
        private int playScore;
        private String rankingGetTime;
        private int rankingNum;
        private String userId;
        private String userName;

        public Self(int rankingNum, int playScore, String userName) {
            this.rankingNum = rankingNum;
            this.playScore = playScore;
            this.userName = userName;
        }

        public void setActivityId(int activityId) {
            this.activityId = activityId;
        }

        public int getActivityId() {
            return activityId;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }

        public String getPic() {
            return pic;
        }

        public void setPlayScore(int playScore) {
            this.playScore = playScore;
        }

        public int getPlayScore() {
            return playScore;
        }

        public void setRankingGetTime(String rankingGetTime) {
            this.rankingGetTime = rankingGetTime;
        }

        public String getRankingGetTime() {
            return rankingGetTime;
        }

        public void setRankingNum(int rankingNum) {
            this.rankingNum = rankingNum;
        }

        public int getRankingNum() {
            return rankingNum;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

    }
}
