package io.github.abolpv.lightdi.scanner;

import io.github.abolpv.lightdi.annotation.Injectable;
import io.github.abolpv.lightdi.exception.ContainerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans classpath for classes with specific annotations.
 * Supports scanning from both file system and JAR files.
 *
 * @author Abolfazl Azizi
 * @since 1.0.0
 */
public class ClassScanner {
    
    private final ClassLoader classLoader;
    
    public ClassScanner() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }
    
    public ClassScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    /**
     * Scans a package for classes annotated with @Injectable.
     *
     * @param packageName the package to scan
     * @return set of injectable classes found
     */
    public Set<Class<?>> scanPackage(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String path = packageName.replace('.', '/');
        
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                
                if ("file".equals(protocol)) {
                    scanDirectory(new File(resource.toURI()), packageName, classes);
                } else if ("jar".equals(protocol)) {
                    scanJar(resource, packageName, classes);
                }
            }
        } catch (Exception e) {
            throw new ContainerException("Failed to scan package: " + packageName, e);
        }
        
        return classes;
    }
    
    /**
     * Scans multiple packages for injectable classes.
     *
     * @param packageNames the packages to scan
     * @return set of injectable classes found
     */
    public Set<Class<?>> scanPackages(String... packageNames) {
        Set<Class<?>> classes = new HashSet<>();
        for (String packageName : packageNames) {
            classes.addAll(scanPackage(packageName));
        }
        return classes;
    }
    
    private void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + 
                    file.getName().substring(0, file.getName().length() - 6);
                addClass(className, classes);
            }
        }
    }
    
    private void scanJar(URL jarUrl, String packageName, Set<Class<?>> classes) {
        String jarPath = jarUrl.getPath();
        // Extract JAR file path from URL like "file:/path/to/file.jar!/package/path"
        int bangIndex = jarPath.indexOf('!');
        if (bangIndex > 0) {
            jarPath = jarPath.substring(5, bangIndex); // Remove "file:" prefix
        }
        
        try (JarFile jar = new JarFile(jarPath)) {
            String packagePath = packageName.replace('.', '/');
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                
                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    addClass(className, classes);
                }
            }
        } catch (IOException e) {
            throw new ContainerException("Failed to scan JAR: " + jarPath, e);
        }
    }
    
    private void addClass(String className, Set<Class<?>> classes) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (clazz.isAnnotationPresent(Injectable.class)) {
                classes.add(clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore classes that cannot be loaded
        } catch (NoClassDefFoundError e) {
            // Ignore classes with missing dependencies
        }
    }
    
    /**
     * Gets all classes in a package (regardless of annotations).
     *
     * @param packageName the package to scan
     * @return list of all classes found
     */
    public List<Class<?>> getAllClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        
        try {
            InputStream stream = classLoader.getResourceAsStream(path);
            if (stream == null) {
                return classes;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".class")) {
                    String className = packageName + "." + 
                        line.substring(0, line.length() - 6);
                    try {
                        classes.add(classLoader.loadClass(className));
                    } catch (ClassNotFoundException e) {
                        // Ignore
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new ContainerException("Failed to scan package: " + packageName, e);
        }
        
        return classes;
    }
}
