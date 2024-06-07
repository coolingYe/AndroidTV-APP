package com.example.meau;

import org.junit.Test;

import static org.junit.Assert.*;

import com.zee.setting.receive.alarm.AlarmSetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testDate() {
        String[] dates =  {"0:53~5:41","6:05~12:00","13:00~13:56","14:00~15:00","15:10~15:42","15:55~16:15","17:00~18:00","22:23~22:59","23:05~23:30"};
        List<String> dateList = new ArrayList<>(Arrays.asList(dates));
        boolean result = AlarmSetter.AlarmUtil.INSTANCE.validateTimeRange("11:29~15:00", (Set<String>) dateList);
        System.out.println(result);
    }
}