package com.gittors.apollo.extend.support.ext;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.model.ConfigChange;

import java.util.List;
import java.util.Properties;

/**
 * 扩展接口：
 *      开放某些 {@link com.ctrip.framework.apollo.internals.DefaultConfig} 接口权限
 *      开放某些 {@link com.ctrip.framework.apollo.internals.AbstractConfig} 接口权限
 *
 * @author zlliu
 * @date 2021/7/6 22:52
 */
public interface ApolloClientExtendConfig extends Config {
    //  =============================== DefaultConfig 扩展点   =========================================
    /**
     * 初始化
     */
    void initialize();

    /**
     * 增加属性回调
     * @param callBack
     */
    void addPropertiesCallBack(PropertiesCallBack callBack);

    /**
     * 更新配置
     * @param newConfigProperties
     * @param sourceType
     */
    void updateConfig(Properties newConfigProperties, ConfigSourceType sourceType);

    /**
     * 设置属性
     * @param key   属性key
     * @param value 属性值
     */
    void setProperty(String key, String value);

    /**
     * 获得配置仓库
     *
     * @return
     */
    ConfigRepository getConfigRepository();

    //  =============================== AbstractConfig 扩展点  ============================================
    /**
     * 获得修改的监听器
     * @return
     */
    List<ConfigChangeListener> getChangeListener();

    List<ConfigChange> calcPropertyChanges(String namespace, Properties previous, Properties current);
}
