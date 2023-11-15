package com.ibuscloud.commons;

/**
 * 配置数据
 */
public class ConfigurationCommonsDataS {
    /**
     *
     * @param data 数据
     * @param bandwidth 平滑度 为0的时候不进行平滑操作
     * @param maxLimit 最大值
     * @param minLimit 最小值
     * @param startIndex 开始索引 选择你想对某个区域进行平滑过度，开始
     * @param endIndex 结束索引 选择你想对某个区域进行平滑过度，结束
     * @return 返回你想配置的数据
     */
    public static double[] smoothDataPlus(double[] data, double bandwidth, double maxLimit, double minLimit, int startIndex, int endIndex) {
        int n = data.length;
        double[] smoothedData = new double[n];

        for (int i = 0; i < n; i++) {
            if (i >= startIndex && i <= endIndex && bandwidth != 0)  {
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
                // 限制整体最大值和最小值

                data[i] = Math.max(minLimit, Math.min(maxLimit, data[i]));

                smoothedData[i] = data[i];
            }
        }

        return smoothedData;
    }
    /**
     *
     * @param data 数据
     * @param bandwidth 平滑度
     * @param startIndex 开始索引
     * @param endIndex 结束索引
     * @return 返回你想配置的数据
     */
    public static double[] smoothDataPlus(double[] data, double bandwidth, int startIndex, int endIndex) {
        int n = data.length;
        double[] smoothedData = new double[n];

        for (int i = 0; i < n; i++) {
            if (i >= startIndex && i <= endIndex && bandwidth != 0) {
                double numerator = 0.00;
                double denominator = 0.00;

                for (int j = 0; j < n; j++) {
                    double weight = loessWeight(i, j, bandwidth);
                    numerator += data[j] * weight;
                    denominator += weight;
                }
                double smoothedValue = numerator / denominator;

                // 限制整体最大值和最小值

                // smoothedValue = Math.max(minLimit, Math.min(maxLimit, smoothedValue));

                smoothedData[i] = smoothedValue;
            } else {
                smoothedData[i] = data[i];
            }
        }

        return smoothedData;
    }

    /**
     * 平滑方法
     * @param i 数据索引
     * @param j 数据索引
     * @param bandwidth 平滑参数
     * @return
     */
    private static double loessWeight(int i, int j, double bandwidth) {
        double u = Math.abs(i - j) / bandwidth;
        double weight = 0.00;

        if (u < 1) {
            weight = (1 - u * u * u) * (1 - u * u * u);
        }

        return weight;
    }

    public static void main(String[] args) {

    }
}
