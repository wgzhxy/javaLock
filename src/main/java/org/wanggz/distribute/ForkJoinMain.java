package org.wanggz.distribute;


import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class ForkJoinMain {

    public static void main(String[] args) {

        //生成task任务
        ProductListGenerator productListGenerator = new ProductListGenerator();
        List<Product> products = productListGenerator.generate(10000);
        Task task = new Task(products, 0, products.size(), 0.2);

        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(task);
        //查看处理情况
        do {
            System.out.printf("Main: Thread Count: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Thread Steal: %d\n", pool.getStealCount());
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!task.isDone());

        pool.shutdown();

        if (task.isCompletedNormally()) {
            System.out.printf("Main: The process has completed normally.\n");
        }
        //是否计算完成
        for (Product product : products) {
            if (product.getPrice() != 12) {
                System.out.printf("Product %s: %f\n", product.getName(), product.getPrice());
            }
        }
        System.out.println("Main: End of the program.\n");
    }
}
