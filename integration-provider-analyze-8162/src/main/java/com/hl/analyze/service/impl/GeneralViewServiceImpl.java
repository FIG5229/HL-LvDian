package com.hl.analyze.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hl.analyze.dao.GeneralViewDao;
import com.hl.analyze.dto.BarGraphDTO;
import com.hl.analyze.dto.LineChartDTO;
import com.hl.analyze.dto.QjnyfdlDataDTO;
import com.hl.analyze.dto.QjnygmDataDTO;
import com.hl.analyze.entity.bo.ChartDataBO;
import com.hl.analyze.entity.bo.HeatMapBO;
import com.hl.analyze.entity.bo.LabelTypeBO;
import com.hl.analyze.entity.bo.NameBO;
import com.hl.analyze.entity.po.GvCleanEnergyPO;
import com.hl.analyze.entity.po.GvRenewableEnergyPO;
import com.hl.analyze.entity.podo.LabelUnitDO;
import com.hl.analyze.entity.podo.LabelValueDO;
import com.hl.analyze.query.BarGraphQuery;
import com.hl.analyze.query.CommonQuery;
import com.hl.analyze.service.GeneralViewService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jason
 * @date 2021/11/10
 */

@Service
public class GeneralViewServiceImpl implements GeneralViewService {

    @Resource
    private GeneralViewDao generalViewDao;

    @Override
    public Map<String, Object> getGeoHeatmapData(CommonQuery commonQuery) {

        List<HeatMapBO> heatmapData = generalViewDao.getGeoHeatmapData(commonQuery);

        Map<String, Object> map = Maps.newHashMap();
        heatmapData.forEach(item -> {
            Map<String, Object> codeValueMap = Maps.newHashMap();
            map.put(item.getOrgName(), codeValueMap);
            codeValueMap.put("code", item.getOrgNo());
            codeValueMap.put("value", item.getValue());
        });

        return map;
    }

    @Override
    public QjnygmDataDTO getQjnygmDataByAreaCode(CommonQuery commonQuery) {
        GvRenewableEnergyPO scaleData = generalViewDao.getQjnygmData(commonQuery);
        return QjnygmDataDTO.builder()
                // ????????????
                .zjrl(new LabelUnitDO(scaleData.getNewEnergyCapacity(), "?????????"))
                // ???????????????
                .ldfgl(new LabelUnitDO(scaleData.getProportion(), "%"))
                // ?????????????????????
                .fbsgfHs(new LabelUnitDO(BigDecimal.valueOf(52251), ""))
                // ???????????????????????????
                .fbsgfZjrl(new LabelUnitDO(scaleData.getDistributedCapacity(), "?????????"))
                // ?????????????????????
                .jzsgfHs(new LabelUnitDO(BigDecimal.valueOf(18), ""))
                // ???????????????????????????
                .jzsgfZjrl(new LabelUnitDO(scaleData.getCentralCapacity(), "?????????"))
                // ????????????
                .fdHs(new LabelUnitDO(BigDecimal.valueOf(29), ""))
                // ??????????????????
                .fdZjrl(new LabelUnitDO(scaleData.getWindPowerCapacity(), "?????????"))
                .build();
    }

    @Override
    public QjnyfdlDataDTO getQjnyfdlDataByAreaCode(CommonQuery commonQuery) {
        GvRenewableEnergyPO gvRenewableEnergy = generalViewDao.getQjnygmData(commonQuery);
        return QjnyfdlDataDTO.builder()
                .list(new ArrayList<LabelUnitDO>() {
                    private static final long serialVersionUID = 77420537020920653L;

                    {
                        add(new LabelTypeBO("?????????????????????", gvRenewableEnergy.getNewEnergyGeneratingPower(), "????????????", "qjnyfdl"));
                        add(new LabelTypeBO("????????????????????????", gvRenewableEnergy.getDistributedGeneratingPower(), "????????????", "fbsgffdl"));
                        add(new LabelTypeBO("???????????????????????????", gvRenewableEnergy.getProportion(), "%", "qjnyfdlzb"));
                        add(new LabelTypeBO("??????????????????????????????", gvRenewableEnergy.getDistributedGeneratingPower()
                                .divide(gvRenewableEnergy.getNewEnergyGeneratingPower(), 3, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)), "%", "fbsgffdlzb"));
                        add(new LabelTypeBO("???????????????", gvRenewableEnergy.getSocialElectricityConsumption(), "????????????", "shydl"));
                    }
                })
                .build();
    }

    @Override
    public BarGraphDTO getQjnygmDetail(BarGraphQuery barGraphQuery) {
        return getBar(barGraphQuery);
    }

    @Override
    public BarGraphDTO getQjnyfdlDetail(BarGraphQuery barGraphQuery) {
        return getBar(barGraphQuery);
    }

    @Override
    public LineChartDTO getQjnyfaqsByAreaCode(CommonQuery commonQuery, Integer type) {

        List<GvCleanEnergyPO> gvCleanEnergyList = generalViewDao.getQjnyfaqsByAreaCode(commonQuery, type);

        LineChartDTO.LineChartDTOBuilder builder = LineChartDTO.builder()
                .tabData(new NameBO(type == 1 ? "????????????" : "?????????"));

        List<LabelValueDO> thermalChartDataList = Lists.newArrayList();
        List<LabelValueDO> biomassChartDataList = Lists.newArrayList();
        List<LabelValueDO> photovoltaicChartDataList = Lists.newArrayList();
        List<LabelValueDO> windChartDataList = Lists.newArrayList();

        // ???????????????
        LinkedList<ChartDataBO> chartDataList = new LinkedList<>();

        if (type == 1) {
            gvCleanEnergyList.forEach(gvCleanEnergy -> {
                String year = gvCleanEnergy.getYear();
                thermalChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getThermalPowerCapacityRatio()));
                biomassChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getBiomassCapacityRatio()));
                photovoltaicChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getPhotovoltaicCapacityRatio()));
                windChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getWindPowerCapacityRatio()));
            });
        } else {
            List<LabelValueDO> outsideChartDataList = Lists.newArrayList();
            gvCleanEnergyList.forEach(gvCleanEnergy -> {
                String year = gvCleanEnergy.getYear();
                thermalChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getThermalRadio()));
                biomassChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getBiomassRadio()));
                outsideChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getOutsideRadio()));
                photovoltaicChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getPhotovoltaicRatio()));
                windChartDataList.add(new LabelValueDO(year, gvCleanEnergy.getWindPowerRatio()));
            });
            chartDataList.add(new ChartDataBO("????????????", true, outsideChartDataList));
        }

        // ????????????????????????2???
        chartDataList.addFirst(new ChartDataBO("??????", true, thermalChartDataList));
        chartDataList.addLast(new ChartDataBO("?????????", true, biomassChartDataList));
        chartDataList.addLast(new ChartDataBO("??????", true, photovoltaicChartDataList));
        chartDataList.addLast(new ChartDataBO("??????", true, windChartDataList));

        return builder
                .chartData(chartDataList)
                .build();
    }

    /**
     * ????????????????????????/????????????????????????
     *
     * @param barGraphQuery ????????????
     * @return BarGraphDTO.class
     */
    private BarGraphDTO getBar(BarGraphQuery barGraphQuery) {
        if (StringUtils.isBlank(barGraphQuery.getType())) {
            return BarGraphDTO.builder().data(Lists.newArrayList()).build();
        }
        Stream<GvRenewableEnergyPO> qjnygmDetailStream = generalViewDao.getQjnygmDetail(barGraphQuery).stream();
        return buildBarGraph(barGraphQuery.getType(), qjnygmDetailStream);
    }

    /**
     * ??????
     *
     * @param type               ??????
     * @param qjnygmDetailStream ??????????????????
     * @return BarGraphDTO.class
     */
    private BarGraphDTO buildBarGraph(String type, Stream<GvRenewableEnergyPO> qjnygmDetailStream) {

        Stream<LabelValueDO> labelValueStream;

        BarGraphDTO.BarGraphDTOBuilder builder = BarGraphDTO.builder()
                .name("")
                .unit("");

        switch (type) {
            // ??????????????????-????????????
            case "qjnygm": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getNewEnergyCapacity()));
                break;
            }
            // ??????????????????-???????????????
            case "fbsgf": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getDistributedCapacity()));
                break;
            }
            // ??????????????????-???????????????
            case "jzsgf": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getCentralCapacity()));
                break;
            }
            // ??????????????????-???????????????
            case "fd": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getWindPowerCapacity()));
                break;
            }
            // ??????????????????-???????????????
            case "ldfgl":
                // ?????????????????????-???????????????????????????
            case "qjnyfdlzb": {
                // ???????????????????????????
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getProportion()));
                break;
            }
            // ?????????????????????-?????????????????????
            case "qjnyfdl": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getNewEnergyGeneratingPower()));
                break;
            }
            // ?????????????????????-????????????????????????
            case "fbsgffdl": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getDistributedGeneratingPower()));
                break;
            }
            // ?????????????????????-??????????????????????????????
            case "fbsgffdlzb": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(),
                                item.getDistributedGeneratingPower()
                                        .divide(item.getNewEnergyGeneratingPower(), 3, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))));
                break;
            }
            // ?????????????????????-???????????????
            case "shydl": {
                labelValueStream = qjnygmDetailStream
                        .map(item -> new LabelValueDO(item.getDistrictName(), item.getSocialElectricityConsumption()));
                break;
            }
            default:
                return builder.data(Lists.newArrayList()).build();

        }

        builder.data(labelValueStream.sorted(Comparator.comparing(LabelValueDO::getValue).reversed())
                .collect(Collectors.toList()));

        return builder.build();
    }

}
