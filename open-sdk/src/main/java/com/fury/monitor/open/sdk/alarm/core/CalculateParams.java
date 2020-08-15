package com.okcoin.cloud.client.demo.alarm.core;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.okcoin.cloud.client.demo.alarm.core.CoreConstants.*;

/**
 * @author : fury
 * @date : 2020/8/7
 */
@Slf4j
@Data
public class CalculateParams {

    private Integer value;

    private String clusterName;

    private String itemName;

    private String tagName = "";

    private Integer numerator = 0;

    private Integer denominator = 0;

    private Algorithm algorithm;

    private CalculateParams() {}

    public static class Builder {

        public static CalculateParams build(final String clusterName, final String item, final Integer value,
            final String tag, final Algorithm algorithm) {

            if (StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(item) || null == value) {
                return null;
            }
            final CalculateParams params = new CalculateParams();
            params.setClusterName(clusterName);
            if (setItemFail(item, params) || setTagFail(tag, params)) {
                return null;
            }
            params.setValue(value);
            params.setAlgorithm(algorithm);
            return params;
        }

        public static CalculateParams buildAvg(final String clusterName, final String item, final Integer numerator,
            final Integer denominator, final String tag, final Algorithm algorithm) {

            if (StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(item) || null == numerator
                || null == denominator) {
                return null;
            }
            final CalculateParams params = new CalculateParams();
            params.setClusterName(clusterName);
            if (setItemFail(item, params) || setTagFail(tag, params)) {
                return null;
            }

            params.setNumerator(numerator);
            params.setDenominator(denominator);
            params.setAlgorithm(algorithm);
            return params;
        }

        private static boolean setTagFail(final String tag, final CalculateParams params) {
            if (StringUtils.isEmpty(tag)) {
                // 标签可以为 null 或空字符串
                return false;
            }
            if (tag.length() <= TAG_MAX_LEN) {
                params.setTagName(tag);
                return false;
            }

            log.error(FURY_MONITOR + "tag={},tag length:{},greater than 127", tag, tag.length());
            return true;
        }

        private static boolean setItemFail(final String item, final CalculateParams params) {
            if (item.length() <= ITEM_MAX_LEN) {
                params.setItemName(item);
                return false;
            }
            // 监控项名称长度不能超过 255
            log.error(FURY_MONITOR + "item={}, item length = {}, greater than 255", item, item.length());
            return true;
        }
    }
}
