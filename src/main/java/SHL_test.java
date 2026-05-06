package main.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class SHL_test {




        public static void main1(String[] args) {
            Scanner sc = new Scanner(System.in);
            double x1 = sc.nextDouble(), y1 = sc.nextDouble(), r1 = sc.nextDouble();
            double x2 = sc.nextDouble(), y2 = sc.nextDouble(), r2 = sc.nextDouble();
            // 错了 Math 也错 sqrt
            double d = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

            double area;

            // 情况1: 分离或外切
            if (d >= r1 + r2) {
                area = 0.0;
            }
            // 情况2: 完全包含
            else if (d <= Math.abs(r1 - r2)) {
                area = Math.PI * Math.pow(Math.min(r1, r2), 2);
            }
            // 情况3: 相交
            else {
                double angle1 = Math.acos((r1 * r1 + d * d - r2 * r2) / (2 * r1 * d));
                double angle2 = Math.acos((r2 * r2 + d * d - r1 * r1) / (2 * r2 * d));

                double sector1 = r1 * r1 * angle1;
                double sector2 = r2 * r2 * angle2;

                double triangle1 = 0.5 * r1 * r1 * Math.sin(2 * angle1);
                double triangle2 = 0.5 * r2 * r2 * Math.sin(2 * angle2);

                area = sector1 + sector2 - triangle1 - triangle2;

            }

            System.out.printf("%.6f\n", area); //分号
            // 或者：System.out.println(String.format("%.6f", area));
        }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());

        // key: resourceId, value: list of [requestTime, endTime]
        Map<Integer, List<int[]>> map = new HashMap<>();

        for (int i = 0; i < n; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int requestTime = Integer.parseInt(st.nextToken());
            int endTime = Integer.parseInt(st.nextToken());
            int resourceId = Integer.parseInt(st.nextToken());

            map.computeIfAbsent(resourceId, k -> new ArrayList<>())
                    .add(new int[]{requestTime, endTime});
        }


        int total = 0;

        // 对每个资源单独贪心
        for (List<int[]> apps : map.values()) {
            // 按结束时间排序
            //
            apps.sort((a, b) -> a[1] - b[1]);

            int lastEnd = -1;
            for (int[] app : apps) {
                if (app[0] >= lastEnd) {
                    total++;
                    lastEnd = app[1];
                }
            }
        }

        System.out.println(total);
    }

}
