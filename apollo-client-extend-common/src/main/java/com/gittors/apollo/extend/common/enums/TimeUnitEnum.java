package com.gittors.apollo.extend.common.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author zlliu
 * @date 2020/8/25 18:41
 */
public enum TimeUnitEnum {
    /**
     * 秒
     */
    SECONDS("seconds",TimeUnit.SECONDS),

    /**
     * 分钟
     */
    MINUTES("minutes", TimeUnit.MINUTES),

    /**
     * 小时
     */
    HOURS("hours", TimeUnit.HOURS),

    /**
     * 天
     */
    DAYS("days", TimeUnit.DAYS);

    TimeUnitEnum(String timeUnitStr, TimeUnit timeUnit) {
        this.timeUnitStr = timeUnitStr;
        this.timeUnit = timeUnit;
    }

    /**
     * 根据timeUnitStr获得TimeUnit
     * @param timeUnitStr
     * @return
     */
    public static TimeUnit getTimeUnit(String timeUnitStr) {
        Optional<TimeUnitEnum> optional =
                Arrays.asList(values()).parallelStream()
                        .filter(unit -> unit.getTimeUnitStr().equals(timeUnitStr))
                        .findFirst();
        if (optional.isPresent()) {
            return optional.get().getTimeUnit();
        } else {
            return MINUTES.getTimeUnit();
        }
    }

    /**
     * TimeUnit字符串
     */
    private String timeUnitStr;

    /**
     * TimeUnit
     */
    private TimeUnit timeUnit;

    public String getTimeUnitStr() {
        return timeUnitStr;
    }

    public void setTimeUnitStr(String timeUnitStr) {
        this.timeUnitStr = timeUnitStr;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
