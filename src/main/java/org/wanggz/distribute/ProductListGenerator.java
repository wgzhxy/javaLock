package org.wanggz.distribute;

import java.util.ArrayList;
import java.util.List;

/**
 * generate a list of random products
 *
 * @author bird
 *         2014年10月7日 下午11:24:47
 */
public class ProductListGenerator {

    public List<Product> generate(int size) {
        List<Product> list = new ArrayList<Product>();
        for (int i = 0; i < size; i++) {
            Product product = new Product();
            product.setName("Product" + i);
            product.setPrice(10);
            list.add(product);
        }
        return list;
    }
}  