package com.gittors.apollo.extend.common.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.util.ResourceUtils;

/**
 * @author zlliu
 * @date 2020/8/25 20:59
 */
public final class EncryptUtils {
    private EncryptUtils() {
    }

    private static final String SALT = "@ApolloClientExtendAdmin@abcdefg!!";

    private static BasicTextEncryptor basicTextEncryptor;

    static {
        String saltStr = null;
        try {
            saltStr = FileUtils.readFileToString(ResourceUtils.getFile("classpath:file/salt.txt"));
        } catch (Exception e) {
        }
        basicTextEncryptor = new BasicTextEncryptor();
        basicTextEncryptor.setPassword(StringUtils.isNotBlank(saltStr) ? saltStr : SALT);
    }

    public static String encrypt(String msg) {
        return basicTextEncryptor.encrypt(msg);
    }

    public static String decrypt(String encryptedMessage) {
        return basicTextEncryptor.decrypt(encryptedMessage);
    }

}
