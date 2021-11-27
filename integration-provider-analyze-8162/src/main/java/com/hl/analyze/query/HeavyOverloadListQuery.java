package com.hl.analyze.query;

import com.hl.analyze.entity.po.HeavyLoad;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 重过载列表查询条件。
 *
 * @see HeavyLoad
 * @author ForteScarlet
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeavyOverloadListQuery extends CommonQuery {

    // 备用

    /**
     * ID
     */
    private Integer id;

    /**
     * 变压器ID
     */
    private String transformerId;

    /**
     * 变压器名称
     */
    private String transformerName;

    /**
     * 最大负载率
     */
    private BigDecimal maxLoadRate;

    /**
     * 平均负载率
     */
    private BigDecimal avgLoadRate;

    /**
     * 供电所名称
     */
    private String company;

    /**
     * 额定容量
     */
    private BigDecimal ratedCapacity;

    /**
     * 设备状态。1：在运
     */
    private Byte deviceStatus;

    /**
     * 是否农网。1：农网，2：城网
     */
    private Byte networkType;

    /**
     * 投运日期
     */
    private Date inUseDate;

    /**
     * 期别
     */
    private String year;

    /**
     * 所属地市名称
     */
    private String cityCompany;

    /**
     * 县公司名称
     */
    private String countyCompany;

    /**
     * 最大电流。
     * =F2*1.44*C2/100
     * =最大负载率*1.44*额定容量/100
     */
    private BigDecimal maxCurrent;

    /**
     * 匹配最小容量
     * =M2/0.8/1.44
     * =最大电流/0.8/1.44
     */
    private BigDecimal matchMinCapacity;


}
