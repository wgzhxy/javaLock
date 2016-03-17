package org.wanggz.classload;

import sun.net.www.ParseUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by guangzhong.wgz on 2016/3/16.
 */
public class MyClassLoader extends ClassLoader {

    private List<URL> urls = new CopyOnWriteArrayList<URL>();

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> c = findClass(name);
        if (c == null) c = super.loadClass(name);
        return c;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {

        Class<?> c = null;
        if (name == null || "".equals(name.trim())) {
            throw new ClassNotFoundException("class not find, class name is null");
        }
        InputStream in = null;
        try {
            in = getResourceAsInputStream(name);
            if (in != null) {
                int blentgh = in.available();
                byte[] b = new byte[blentgh];
                in.read(b);
                c = this.defineClass(name, b, 0, blentgh);
            }
        } catch (Throwable e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable e) {
            }
        }
        return c;
    }

    public InputStream getResourceAsInputStream(String name) throws ClassNotFoundException {
        try {
            String path = "";
            if (name != null && !"".equals(name.trim())) {
                path = name.replace('.', '/').concat(".class");
            }
            for (URL url : urls) {
                if (url.getProtocol().equals(UrlProtocol.FILE.value())) {
                    URL fileUrl = new URL(url, ParseUtil.encodePath(path, false));
                    // return new FileInputStream(new File(fileUrl.toURI()));
                    return fileUrl.openConnection().getInputStream();
                }
            }
        } catch (Throwable e) {
            throw new ClassNotFoundException(String.format("%s is not find, msg %s", new Object[]{name, e.getMessage()}), e.getCause());
        }
        return null;
    }

    public void addUrl(URL url) {
        urls.add(url);
    }

    public void removeUrl(int index) {
        urls.remove(index);
    }

    public List<URL> getUrls() {
        return urls;
    }

    enum UrlProtocol {

        FILE("file"),
        HTTP("http");

        UrlProtocol(String value) {
            this.value = value;
        }


        String value;

        public String value() {
            return this.value;
        }

    }
}
