package com.ibuscloud.commons;

import java.math.BigDecimal;

/**
 * 数据平滑方法
 */
public class LoessSmoothing {

    /**
     *
     * @param data 数据
     * @param bandwidth 平滑度
     * @param maxLimit 最大值
     * @param minLimit 最小值
     * @param jitterAmplitude 抖动幅度
     * @param jitterFrequency 抖动频率
     * @return
     */
    public static double[] smoothData(double[] data, double bandwidth, double maxLimit, double minLimit, double jitterAmplitude, double jitterFrequency,int formatNum) {
        int n = data.length;
        double[] smoothedData = new double[n];

        for (int i = 0; i < n; i++) {
            double numerator = 0.00;
            double denominator = 0.00;

            for (int j = 0; j < n; j++) {
                double weight = loessWeight(i, j, bandwidth);
                numerator += data[j] * weight;
                denominator += weight;
            }

            double smoothedValue = numerator / denominator;

            // 添加抖动
            double jitter = jitterAmplitude * Math.sin(jitterFrequency * i);
            smoothedValue += jitter;
            // 限制整体最大值和最小值
            smoothedValue = Math.max(minLimit, Math.min(maxLimit, smoothedValue));
            // 使用 BigDecimal 设置精度
            BigDecimal bd = new BigDecimal(smoothedValue);
            bd = bd.setScale(formatNum, BigDecimal.ROUND_HALF_UP); // 进位取舍

            smoothedData[i] = bd.doubleValue();
        }

        return smoothedData;
    }

    private static double loessWeight(int i, int j, double bandwidth) {
        double u = Math.abs(i - j) / bandwidth;
        double weight = 0.00;

        if (u < 1) {
            weight = (1 - u * u * u) * (1 - u * u * u);
        }

        return weight;
    }
    /**
     *
     * @param data 数据
     * @param bandwidth 平滑度
     * @param maxLimit 最大值
     * @param minLimit 最小值
     * @param jitterAmplitude 抖动幅度
     * @param jitterFrequency 抖动频率
     * @param startIndex 开始索引
     * @param endIndex 结束索引
     * @return
     */
    public static double[] smoothDataPlus(double[] data, double bandwidth, double maxLimit, double minLimit, double jitterAmplitude, double jitterFrequency, int startIndex, int endIndex,int formatNum) {
        int n = data.length;
        double[] smoothedData = new double[n];

        for (int i = 0; i < n; i++) {
            if (i >= startIndex && i <= endIndex) {
                double numerator = 0.00;
                double denominator = 0.00;

                for (int j = 0; j < n; j++) {
                    double weight = loessWeight(i, j, bandwidth);
                    numerator += data[j] * weight;
                    denominator += weight;
                }

                double smoothedValue = numerator / denominator;


                // 添加抖动
                double jitter = jitterAmplitude * Math.sin(jitterFrequency * i);
                smoothedValue += jitter;
                // 限制整体最大值和最小值
                smoothedValue = Math.max(minLimit, Math.min(maxLimit, smoothedValue));
                // 使用 BigDecimal 设置精度
                BigDecimal bd = new BigDecimal(smoothedValue);
                bd = bd.setScale(formatNum, BigDecimal.ROUND_HALF_UP); // 进位取舍

                smoothedData[i] = bd.doubleValue();
            } else {
                smoothedData[i] = data[i];
            }
        }

        return smoothedData;
    }
    /**
     *
     * @param data 数据
     * @param bandwidth 平滑度
     * @param maxLimit 最大值
     * @param minLimit 最小值
     * @param startIndex 开始索引
     * @param endIndex 结束索引
     * @return
     */
    public static double[] smoothDataPlus(double[] data, double bandwidth, double maxLimit, double minLimit, int startIndex, int endIndex) {
        int n = data.length;
        double[] smoothedData = new double[n];

        for (int i = 0; i < n; i++) {
            if (i >= startIndex && i <= endIndex) {
                double numerator = 0.00;
                double denominator = 0.00;

                for (int j = 0; j < n; j++) {
                    double weight = loessWeight(i, j, bandwidth);
                    numerator += data[j] * weight;
                    denominator += weight;
                }
                double smoothedValue = numerator / denominator;

                // 限制整体最大值和最小值

                smoothedValue = Math.max(minLimit, Math.min(maxLimit, smoothedValue));

                smoothedData[i] = smoothedValue;
            } else {
                smoothedData[i] = data[i];
            }
        }

        return smoothedData;
    }

    public static void main(String[] args) {
        double[] data = { 27.23,23.55 };
        double bandwidth = 2.0;
        double maxLimit = 300.00; // 最大值限制
        double minLimit = 20;  // 最小值限制
        double jitterAmplitude = 5.00; // 抖动幅度
        double jitterFrequency = 0.10; // 抖动频率

        double[] smoothedData = smoothData(data, bandwidth, maxLimit, minLimit, jitterAmplitude, jitterFrequency,2);

        for (int i = 0; i < data.length; i++) {
            System.out.println("data=" + data[i] + ", smoothed=" + smoothedData[i]);
        }
    }
}