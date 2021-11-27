package com.hl.analyze.utils;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class AxisUtil {

    public static double pi = 3.1415926535897932384626;
    public static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;
    public static String LINE_SPLIT = "\\|\\|";
    public static String LINE_SPLIT_AXIS = ";";
    public static String LINE_SPLIT_COORDINATE = ",";

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    public static double[] transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new double[]{mgLat, mgLon};
    }

    /**
     * 判断是否在中国
     *
     * @param lat
     * @param lon
     * @return
     */
    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347) {
            return true;
        }
        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }

    /**
     * 84 ==》 高德
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] gps84_To_Gcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new double[]{mgLat, mgLon};
    }

    /**
     * 高德 ==》 84
     *
     * @param lon * @param lat * @return
     */
    public static double[] gcj02_To_Gps84(double lat, double lon) {
        double[] gps = transform(lat, lon);
        double lontitude = lon * 2 - gps[1];
        double latitude = lat * 2 - gps[0];
        return new double[]{latitude, lontitude};
    }

    /**
     * 高德 == 》 百度
     *
     * @param lat
     * @param lon
     */
    public static double[] gcj02_To_Bd09(double lat, double lon) {
        double x = lon, y = lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        double[] gps = {tempLat, tempLon};
        return gps;
    }

    /**
     * 百度 == 》 高德
     *
     * @param lat
     * @param lon
     */
    public static double[] bd09_To_Gcj02(double lat, double lon) {
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta);
        double tempLat = z * Math.sin(theta);
        double[] gps = {tempLat, tempLon};
        return gps;
    }

    /**
     * 84 == 》 百度
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] gps84_To_bd09(double lat, double lon) {
        double[] gcj02 = gps84_To_Gcj02(lat, lon);
        double[] bd09 = gcj02_To_Bd09(gcj02[0], gcj02[1]);
        return bd09;
    }

    /**
     * 百度 == 》 84
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] bd09_To_gps84(double lat, double lon) {
        double[] gcj02 = bd09_To_Gcj02(lat, lon);
        double[] gps84 = gcj02_To_Gps84(gcj02[0], gcj02[1]);
        //保留小数点后六位
        gps84[0] = retain6(gps84[0]);
        gps84[1] = retain6(gps84[1]);
        return gps84;
    }

    public static List<List<List<String>>> buildLineLocation(String line) {
        if (StringUtils.containsNone(line, "\\|\\|")) {
            return Lists.newArrayList();
        }
        List<List<List<String>>> list = Lists.newArrayList();
        String[] split = line.split(LINE_SPLIT);
        for (String value : split) {
            if (StringUtils.containsNone(value, LINE_SPLIT_AXIS)) {
                continue;
            }
            List<List<String>> axis = Lists.newArrayList();
            for (String s : value.split(LINE_SPLIT_AXIS)) {
                if (StringUtils.containsNone(value, LINE_SPLIT_COORDINATE)) {
                    continue;
                }
                axis.add(Arrays.asList(s.split(LINE_SPLIT_COORDINATE)));
            }
            list.add(axis);
        }
        return list;
    }

    public static List<BigDecimal> buildAxis(String longitude, String latitude) {
        if (StringUtils.isAllBlank(longitude, latitude)) {
            return Lists.newArrayList();
        }
        return Arrays.asList(new BigDecimal(longitude), new BigDecimal(latitude));
    }

    /**
     * 保留小数点后六位
     * @param num
     * @return
     */
    private static double retain6(double num) {
        String result = String.format("%.6f", num);
        return Double.valueOf(result);
    }

    /**
     * 线路坐标转换
     * @param lineAxis
     * @return
     */
    public static List<List<List<String>>> lineConversion(String lineAxis) {
        if (StringUtils.isBlank(lineAxis)) {
            return Lists.newArrayList();
        }
        List<List<List<String>>> list = new LinkedList<>();
        String[] split = lineAxis.substring(17, lineAxis.length() - 2).split("\\), \\(");
        for (String s : split) {
            List<List<String>> line = new ArrayList<>();
            String[] split1 = s.split(", ");
            for (String s1 : split1) {
                String[] s2 = s1.split(" ");
                List<String> axisList = new ArrayList<>(Arrays.asList(s2));
                line.add(axisList);
            }
            list.add(line);
        }
        return list;
    }

    /**
     * 线路坐标转换
     * @param lineAxis
     * @return
     */
    public static List<List<List<BigDecimal>>> areaConversionBig(String lineAxis) {
        if (StringUtils.isBlank(lineAxis)) {
            return Lists.newArrayList();
        }
        List<List<List<BigDecimal>>> list = new LinkedList<>();
        String[] split = lineAxis.substring(3, lineAxis.length() - 3).split("],\\[");
        List<List<BigDecimal>> line = new ArrayList<>();
        for (String s : split) {
            String[] split1 = s.split(",");
            BigDecimal[] daxis = new BigDecimal[2];
            String trim0 = split1[0].trim();
            String trim1 = split1[1].trim();
            if (StringUtils.isNotBlank(trim0)) {
                daxis[0] = new BigDecimal(trim0);
            }
            if (StringUtils.isNotBlank(trim1)) {
                daxis[1] = new BigDecimal(trim1);
            }
            List<BigDecimal> axisList = new ArrayList<>(Arrays.asList(daxis));
            line.add(axisList);
        }
        list.add(line);
        return list;
    }

    /**
     * 线路坐标转换
     * @param lineAxis
     * @return
     */
    public static List<List<List<String>>> areaConversion(String lineAxis) {
        if (StringUtils.isBlank(lineAxis)) {
            return Lists.newArrayList();
        }
        List<List<List<String>>> list = new LinkedList<>();
        String[] split = lineAxis.substring(3, lineAxis.length() - 3).split("],\\[");
        List<List<String>> line = new ArrayList<>();
        for (String s : split) {
            String[] split1 = s.split(",");
            List<String> axisList = new ArrayList<>(Arrays.asList(split1));
            line.add(axisList);
        }
        list.add(line);
        return list;
    }

    /**
     *oracle.sql.Clob类型转换成String类型
     */
    public static String ClobToString(Clob clob) {

        String reString = "";
        Reader is = null;
        BufferedReader br = null;
        try {
            // 得到流
            is = clob.getCharacterStream();
            br = new BufferedReader(is);
            String s = br.readLine();
            StringBuffer sb = new StringBuffer();
            while (s != null) {
                // 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
                sb.append(s);
                s = br.readLine();
            }
            reString = sb.toString();
        } catch (Exception e) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
            }
        }

        return reString;
    }

}
