package com.ibuscloud.commons;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 造数据的方法工具类
 */
public class CreateCommonsDataS {
    /**
     * @param minLimit  最小值
     * @param maxLimit  最大值
     * @param formatNum 保留几位小数
     * @return 返回你所需要的数据
     * @apiNote 创建单个值的数据
     */
    @Deprecated
    public static Object createDataByRandom(double minLimit, double maxLimit, int formatNum) {
        Random random = new Random();
        double[] data = {(minLimit + (maxLimit - minLimit) * random.nextDouble()), minLimit + (maxLimit - minLimit) * random.nextDouble()};
        double bandwidth = 2.0;
        double jitterAmplitude = 0; // 抖动幅度
        double jitterFrequency = 0; // 抖动频率

        double[] smoothedData = LoessSmoothing.smoothData(data, bandwidth, maxLimit, minLimit, jitterAmplitude, jitterFrequency, formatNum);
        if (smoothedData.length == 0) {
            return 0.0; // 如果数组为空，返回 0.0 或者你认为合适地默认值
        }
        double sum = 0.0;
        for (double dataPoint : smoothedData) {
            sum += dataPoint;
        }
        //平均值
        double sumAverage = sum / smoothedData.length;
        // 使用 BigDecimal 设置精度
        BigDecimal bd = new BigDecimal(sumAverage);
        bd = bd.setScale(formatNum, BigDecimal.ROUND_HALF_UP); // 进位取舍
        return bd.doubleValue();
    }

    /**
     * @param minLimit 最小值
     * @param maxLimit 最大值
     * @param sums     数据总数
     * @param n        平滑次数
     * @return 数据
     */
    @Deprecated
    public static double[] createIndicatorDataBySumsN(double minLimit, double maxLimit, int sums, int n, int formatNum) {
        //默认数据
        double bandwidth = 2.0;
        double jitterAmplitude = 0; // 抖动幅度
        double jitterFrequency = 0; // 抖动频率
        //随机数产生
        Random random = new Random();

        double[] origin = new double[sums];
        //随机数赋值
        for (int i = 0; i < origin.length; i++) {
            origin[i] = minLimit + (maxLimit - minLimit) * random.nextDouble();
        }
        //数据平滑方法
        double[] smoothedData = origin;
        //循环次数
        for (int i = 0; i < n; i++) {

            smoothedData = LoessSmoothing.smoothData(smoothedData, bandwidth, maxLimit, minLimit, jitterAmplitude, jitterFrequency, formatNum);
        }
        if (smoothedData.length == 0) {
            // 如果数组为空，返回 0.0 或者你认为合适地默认值
            return new double[1];
        }


        return smoothedData;
    }

    /**
     * 暂时作为造数据的测试数据
     *
     * @param minLimit        起始值
     * @param maxLimit        最大值
     * @param bandwidth       平滑窗口 2.0初始值平滑
     * @param jitterAmplitude 抖动幅度 1为初始值
     * @param jitterFrequency 抖动频率 1为初始值
     * @param n               循环次数
     * @param sums            数据总量
     * @return
     */
    @Deprecated
    public static double[] createIndicatorDataAll(double minLimit, double maxLimit, double bandwidth, double jitterAmplitude, double jitterFrequency, int sums, int n, int formatNum) {
        //随机数产生
        Random random = new Random();

        double[] origin = new double[sums];
        //随机数赋值
        //随机数赋值
        for (int i = 0; i < origin.length; i++) {
            origin[i] = minLimit + (maxLimit - minLimit) * random.nextDouble();
        }
        //数据平滑方法
        double[] smoothedData = origin;
        //循环次数
        for (int i = 0; i < n; i++) {
            smoothedData = LoessSmoothing.smoothData(smoothedData, bandwidth, maxLimit, minLimit, jitterAmplitude, jitterFrequency, formatNum);
        }
        if (smoothedData.length == 0) {
            // 如果数组为空，返回 0.0 或者你认为合适地默认值
            return new double[1];
        }
        return smoothedData;
    }

    /**
     * 根据函数造数据
     *
     * @param inputData        原函数
     * @param factor           动态因子
     * @param num              降低或者太高
     * @param polyCoefficients 多项式拟合参数
     * @param rands            波动范围
     * @return 你所需要的数据
     */
    // TODO 根据函数造数据
    public static double[] generateDataPlusByRand(double[] inputData, double factor, double num, double[] polyCoefficients, double rands) {
        // 检查输入数据是否为空或长度为0，如果是则返回空数组
        if (inputData == null || inputData.length == 0) {
            return new double[0];
        }

        // 计算原始数据的最大值和最小值
        double maxInput = Double.MIN_VALUE;
        double minInput = Double.MAX_VALUE;
        for (double value : inputData) {
            if (value > maxInput) {
                maxInput = value;
            }
            if (value < minInput) {
                minInput = value;
            }
        }

        // 创建一个伪随机数生成器，使用因子作为种子
        Random rand = new Random(Double.doubleToLongBits(factor));
        // Random rand = new Random((long) factor);
        // 创建一个数组来存储生成的数据
        double[] generatedData = new double[inputData.length];

        // 计算多项式拟合的系数
        // 使用 Apache Commons Math 库进行多项式拟合
        // double[] polyCoefficients = fitPolynomial(inputData, 8);
        // 遍历输入数据的每个元素，生成数据并保持原始数据特征
        for (int i = 0; i < inputData.length; i++) {
            if (inputData[i] == 0) {
                generatedData[i] = inputData[i];
                continue;
            }
            // 计算多项式拟合值
            double fittedValue = polyval(polyCoefficients, i);

            // 根据原始数据的分布特征添加噪声
            double noise = generateNoisePlus(rands, rand);

            double generatedValue = fittedValue + noise + num;

            // 确保生成的数据在原始数据范围内
            generatedValue = Math.min(maxInput, Math.max(minInput, generatedValue));

            generatedData[i] = generatedValue;
        }

        // 返回生成的数据
        return generatedData;
    }

    /**
     * 根据函数造数据
     *
     * @param inputData        原函数
     * @param factor           动态因子
     * @param num              降低或者太高
     * @param polyCoefficients 多项式拟合参数
     * @return 你所需要的数据
     */
    // TODO 根据函数造数据
    public static double[] generateDataPlus(double[] inputData, double factor, double num, double[] polyCoefficients) {
        // 检查输入数据是否为空或长度为0，如果是则返回空数组
        if (inputData == null || inputData.length == 0) {
            return new double[0];
        }

        // 计算原始数据的最大值和最小值
        double maxInput = Double.MIN_VALUE;
        double minInput = Double.MAX_VALUE;
        for (double value : inputData) {
            if (value > maxInput) {
                maxInput = value;
            }
            if (value < minInput) {
                minInput = value;
            }
        }

        // 创建一个伪随机数生成器，使用因子作为种子
        Random rand = new Random(Double.doubleToLongBits(factor));
        // Random rand = new Random((long) factor);
        // 创建一个数组来存储生成的数据
        double[] generatedData = new double[inputData.length];

        // 计算多项式拟合的系数
        // 使用 Apache Commons Math 库进行多项式拟合
        // double[] polyCoefficients = fitPolynomial(inputData, 8);
        // 遍历输入数据的每个元素，生成数据并保持原始数据特征
        for (int i = 0; i < inputData.length; i++) {
            if (inputData[i] == 0) {
                generatedData[i] = inputData[i];
                continue;
            }
            // 计算多项式拟合值
            double fittedValue = polyval(polyCoefficients, i);

            // 根据原始数据的分布特征添加噪声
            double noise = generateNoiseByFactor(inputData, factor, rand);

            double generatedValue = fittedValue + noise + num;

            // 确保生成的数据在原始数据范围内
            generatedValue = Math.min(maxInput, Math.max(minInput, generatedValue));

            generatedData[i] = generatedValue;
        }

        // 返回生成的数据
        return generatedData;
    }
    // 定义一个函数，根据给定因子生成数据

    /**
     * @param inputData 原函数
     * @param factor    动态因子，传入0的时候表示特别随机，不等于0的时候就你传入什么返回的结果就是一定的
     * @param num       振幅变化 0-num
     * @param maxInput  最小值
     * @param minInput  最大值
     * @return
     */
    // TODO 根据函数造数据
    public static double[] generateDataBy(double[] inputData, long factor, double num, double maxInput, double minInput) {
        // 检查输入数据是否为空或长度为0，如果是则返回空数组
        if (inputData == null || inputData.length == 0) {
            return new double[0];
        }
        Random rand = new Random();
        // 创建一个伪随机数生成器，使用因子作为种子
        if (factor != 0) {
            rand = new Random(factor);
        }
        // 创建一个数组来存储生成的数据
        double[] generatedData = new double[inputData.length];
        // 计算多项式拟合的系数
        // 使用 Apache Commons Math 库进行多项式拟合
        // double[] polyCoefficients = fitPolynomial(inputData, 8);
        // 遍历输入数据的每个元素，生成数据并保持原始数据特征
        for (int i = 0; i < inputData.length; i++) {

            generatedData[i] = inputData[i];

            double noise = rand.nextDouble() * num;

            // 根据原始数据的分布特征添加噪声
            if (rand.nextBoolean()) {
                noise = -noise;
            }

            double generatedValue = noise + generatedData[i];

            // 确保生成的数据在原始数据范围内
            generatedValue = Math.min(maxInput, Math.max(minInput, generatedValue));

            generatedData[i] = generatedValue;
        }

        // 返回生成的数据
        return generatedData;
    }

    public static double[] generateDataByNoMinMax(double[] inputData, long factor, double num) {
        // 检查输入数据是否为空或长度为0，如果是则返回空数组
        if (inputData == null || inputData.length == 0) {
            return new double[0];
        }

        // 计算原始数据的最大值和最小值
        double maxInput = Double.MIN_VALUE;
        double minInput = Double.MAX_VALUE;
        for (double value : inputData) {
            if (value > maxInput) {
                maxInput = value;
            }
            if (value < minInput) {
                minInput = value;
            }
        }

        Random rand = new Random();
        // 创建一个伪随机数生成器，使用因子作为种子
        if (factor != 0) {
            rand = new Random(factor);
        }
        // 创建一个数组来存储生成的数据
        double[] generatedData = new double[inputData.length];
        // 计算多项式拟合的系数
        // 使用 Apache Commons Math 库进行多项式拟合
        // double[] polyCoefficients = fitPolynomial(inputData, 8);
        // 遍历输入数据的每个元素，生成数据并保持原始数据特征
        for (int i = 0; i < inputData.length; i++) {

            generatedData[i] = inputData[i];

            double noise = rand.nextDouble() * num;

            // 根据原始数据的分布特征添加噪声
            if (rand.nextBoolean()) {
                noise = -noise;
            }

            double generatedValue = noise + generatedData[i];

            // 确保生成的数据在原始数据范围内
            generatedValue = Math.min(maxInput, Math.max(minInput, generatedValue));

            generatedData[i] = generatedValue;
        }

        // 返回生成的数据
        return generatedData;
    }

    /**
     * @param inputData 数据样式
     * @param factor    数据因子
     * @param num       抬高或者降低曲线
     * @return
     * @deprecated
     */
    @Deprecated
    public static double[] generateData(double[] inputData, double factor, double num, double[] polyCoefficients) {
        // 检查输入数据是否为空或长度为0，如果是则返回空数组
        if (inputData == null || inputData.length == 0) {
            return new double[0];
        }

        // 计算原始数据的最大值和最小值
        double maxInput = Double.MIN_VALUE;
        double minInput = Double.MAX_VALUE;
        for (double value : inputData) {
            if (value > maxInput) {
                maxInput = value;
            }
            if (value < minInput) {
                minInput = value;
            }
        }

        // 创建一个伪随机数生成器，使用因子作为种子
        Random rand = new Random(Double.doubleToLongBits(factor));
        // Random rand = new Random((long) factor);
        // 创建一个数组来存储生成的数据
        double[] generatedData = new double[inputData.length];

        // 计算多项式拟合的系数
        // 使用 Apache Commons Math 库进行多项式拟合
        // double[] polyCoefficients = fitPolynomial(inputData, 8);
        // 遍历输入数据的每个元素，生成数据并保持原始数据特征
        for (int i = 0; i < inputData.length; i++) {
            if (inputData[i] == 0) {
                generatedData[i] = inputData[i];
                continue;
            }


            // 根据原始数据的分布特征添加噪声
            double noise = generateNoise(inputData, factor, rand);

            double generatedValue = inputData[i] + noise + num;

            // 确保生成的数据在原始数据范围内
            generatedValue = Math.min(maxInput, Math.max(minInput, generatedValue));

            generatedData[i] = generatedValue;
        }

        // 返回生成的数据
        return generatedData;
    }

    /**
     * 多项式拟合函数
     *
     * @param data   原数据
     * @param degree 阶乘
     * @return 返回的是阶乘系数 例如 a+（a1）x+（a2）x^2;  那么这就是二阶  然后返回的系数就是三个
     */
    // TODO 多项式拟合函数
    public static double[] fitPolynomial(double[] data, int degree) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < data.length; i++) {
            obs.add((double) i, data[i]);
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        return fitter.fit(obs.toList());
    }

    /**
     * @param coefficients 多项式拟合的系数
     * @param x            取哪一个值
     * @return 返回某个值
     */
    // TODO 多项式求值函数
    public static double polyval(double[] coefficients, double x) {
        double result = 0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }

    /**
     * @param inputData 输入的数据
     * @param factor    因子
     * @param rand      伪随机数
     * @return 返回噪声数
     * @apiNote 生成噪声函数，根据原始数据的分布特征
     */
    private static double generateNoise(double[] inputData, double factor, Random rand) {
        int maxPlace = getMaxPlace(inputData);
        // 根据占比最多的位生成噪声，使用 factor 控制噪声的幅度
        double noise = (rand.nextDouble() * 0.34 * Math.pow(10, maxPlace - 1));
        // 引入正负号
        if (rand.nextBoolean()) {
            noise = -noise;
        }
        return noise;
    }

    /**
     * @param rands 波动范围
     * @param rand  伪随机数
     * @return 返回噪声数
     * @apiNote 生成噪声函数，根据原始数据的分布特征
     */
    private static double generateNoisePlus(double rands, Random rand) {
        // 根据占比最多的位生成噪声，使用 factor 控制噪声的幅度
        double noise = (rand.nextDouble() * 0.34 * rands);
        // 引入正负号
        if (rand.nextBoolean()) {
            noise = -noise;
        }
        return noise;
    }

    /**
     * @param inputData 输入的数据
     * @param factor    因子
     * @param rand      伪随机数
     * @return 返回噪声数
     * @apiNote 生成噪声函数，根据原始数据的分布特征
     * @deprecated
     */
    @Deprecated
    private static double generateNoiseByFactor(double[] inputData, double factor, Random rand) {
        int maxPlace = getMaxPlace(inputData);
        // 根据占比最多的位生成噪声，使用 factor 控制噪声的幅度
        double noise = (rand.nextDouble() * factor);
        // 引入正负号
        if (rand.nextBoolean()) {
            noise = -noise;
        }
        return noise;
    }

    /**
     * 统计几位数占比最多
     *
     * @param inputData 原数据
     * @return 返回最大占比的位数
     */
    public static int getMaxPlace(double[] inputData) {
        // 统计原始数据中每个数据的位数
        int[] placeCounts = new int[10]; // 假设最多有10位
        int maxPlace = 0; // 记录占比最多的位
        int maxCount = 0;

        for (double value : inputData) {
            int intValue = (int) value;
            //记录位数
            int place = 0;
            while (intValue > 0) {
                intValue /= 10;
                place++;
            }
            placeCounts[place]++;
            if (placeCounts[place] > maxCount) {
                maxCount = placeCounts[place];
                maxPlace = place;
            }
        }
        return maxPlace;
    }

    /**
     * @param number        总数
     * @param proportions   原比例
     * @param decimalPlaces 保留几位小数
     * @param num           伪随机数的动态因子
     * @return 返回经过方法的比例占比
     */
    public static double[] distributeProportionally(double number, double[] proportions, int decimalPlaces, double num, double DataRate) {
        if (number <= 0 || proportions == null || proportions.length == 0 || decimalPlaces < 0) {
            throw new IllegalArgumentException("无效的输入");
        }

        // Random random = new Random(Double.doubleToLongBits(num));
        Random random = new Random((long) num);
        //声明之后的数据占比空间
        double[] distributed = new double[proportions.length];
        //设置计算占比总数
        double sum = 0;
        //存放最终的总数与真实做对比
        double sumPlus = 0;
        //此数据的最大值
        double max = 0;
        //此数据最大值索引
        int maxIndex = 0;
        // 控制小数位数
        DecimalFormat df = new DecimalFormat();
        df.setRoundingMode(RoundingMode.HALF_UP); // 使用四舍五入
        df.setMaximumFractionDigits(decimalPlaces);
        String strNumber = "";
        //得到最大占比的位数众数

        // 初始化数组，给每个位置分配一个随机值
        for (int i = 0; i < proportions.length; i++) {
            if (proportions[i] == 0) {
                continue;
            }
            double randValue = random.nextDouble();
            if (random.nextBoolean()) {
                randValue = -randValue;
            }
            proportions[i] = Math.abs(randValue * DataRate) + proportions[i];

            sum += proportions[i];
        }

        // 缩放分配的值以满足给定的比例，同时保持总和不变
        for (int i = 0; i < proportions.length; i++) {
            String strNum = df.format((proportions[i] / sum) * number);
            if (strNum.contains(",")) {
                strNum = strNum.replace(",", "");
            }
            distributed[i] = Double.parseDouble(strNum);
        }
        //保持总数尽量不变
        for (int i = 0; i < distributed.length; i++) {
            double v = distributed[i];
            if (v > max) {
                max = v;
                maxIndex = i;
            }
            sumPlus += v;
        }
        if (sumPlus != number) {
            distributed[maxIndex] += (number - sumPlus);
        }

        return distributed;
    }

    public static void main(String[] args) {

        double[] doubles = distributeProportionally(6, new double[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 8}, 0, 26, 1);
        List<Double> collect = Arrays.stream(doubles).boxed().collect(Collectors.toList());
        System.out.println(collect);
    }
}
