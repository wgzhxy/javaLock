package org.wanggz.distribute;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public class Task extends RecursiveAction {

    private static final long serialVersionUID = 1L;

    // These attributes will determine the block of products this task has to  
    // process.  
    private List<Product> products;
    private int first;
    private int last;

    // store the increment of the price of the products  
    private double increment;

    public Task(List<Product> products, int first, int last, double increment) {
        super();
        this.products = products;
        this.first = first;
        this.last = last;
        this.increment = increment;
    }

    @Override
    protected void compute() {
        if (last - first < 10) {
            updatePrices();
        } else {
            int middle = (first + last) / 2;
            System.out.printf("Task: Pending tasks:%s\n", getQueuedTaskCount());
            Task t1 = new Task(products, first, middle + 1, increment);
            Task t2 = new Task(products, middle + 1, last, increment);
            invokeAll(t1, t2);
        }
    }

    private void updatePrices() {
        for (int i = first; i < last; i++) {
            Product product = products.get(i);
            product.setPrice(product.getPrice() * (1 + increment));
        }
    }
}  