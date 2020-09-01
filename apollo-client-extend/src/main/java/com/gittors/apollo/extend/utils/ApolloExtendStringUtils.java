package com.gittors.apollo.extend.utils;

import com.gittors.apollo.extend.common.constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zlliu
 * @date 2020/7/22 9:41
 */
public final class ApolloExtendStringUtils {
    private ApolloExtendStringUtils() {
    }

    /**
     * 格式化字符串
     * @param str   字符串
     * @param prefix    前缀
     * @param suffix    后缀
     * @return
     */
    public static String format(String str, String prefix, String suffix) {
        if (StringUtils.isBlank(str)) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(prefix) && StringUtils.isNotBlank(suffix)) {
            return String.format("%s-%s-%s", prefix, str, suffix);
        } else if (StringUtils.isBlank(prefix) && StringUtils.isNotBlank(suffix)) {
            return String.format("%s-%s", str, suffix);
        } else if (StringUtils.isBlank(suffix) && StringUtils.isNotBlank(prefix)) {
            return String.format("%s-%s", prefix, str);
        }
        return CommonConstant.DEFAULT_FORMAT_STR;
    }

}
