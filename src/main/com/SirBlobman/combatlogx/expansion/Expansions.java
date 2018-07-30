package com.SirBlobman.combatlogx.expansion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.SirBlobman.combatlogx.CombatLogX;
import com.SirBlobman.combatlogx.utility.Util;
import com.SirBlobman.combatlogx.utility.WordUtil;

public class Expansions {
    private static final File FOLDER = CombatLogX.FOLDER;
    public static final File EXPAND = new File(FOLDER, "expansions");
    private static HashMap<String, CLXExpansion> EXPANSIONS = Util.newMap();

    /**
     * Register your expansion
     * 
     * @param clazz
     *            Your class file (should implement {@link CLXExpansion})
     */
    public static boolean loadExpansion(Class<?> clazz) {
        try {
            Class<?>[] cc = clazz.getInterfaces();
            boolean reg = false;
            for (Class<?> c : cc) {
                if (c.equals(CLXExpansion.class)) {
                    CLXExpansion clxe = (CLXExpansion) clazz.newInstance();
                    String n = clxe.getName();
                    String v = clxe.getVersion();
                    String msg = Util.format("Loading expansion '%s v%s'", n, v);
                    Util.print(msg);
                    clxe.enable();
                    Util.print("\n");
                    EXPANSIONS.put(n, clxe);
                    reg = true;
                    break;
                } else
                    continue;
            }

            return reg;
        } catch (Throwable ex) {
            String error = "Failed to load expansion:";
            Util.print(error);
            ex.printStackTrace();
            return false;
        }
    }
    
    public static List<CLXExpansion> getExpansions() {
        List<CLXExpansion> list = Util.newList(EXPANSIONS.values());
        return list;
    }

    @SuppressWarnings("unchecked")
    public static void onDisable() {
        Map<String, CLXExpansion> clone = (HashMap<String, CLXExpansion>) EXPANSIONS.clone();
        EXPANSIONS.clear();
        for (Entry<String, CLXExpansion> e : clone.entrySet()) {
            CLXExpansion ex = e.getValue();
            ex.disable();
        }
    }

    public static void loadExpansions() {
        try {
            if (!EXPAND.exists())
                EXPAND.mkdirs();
            File[] files = EXPAND.listFiles();
            Arrays.stream(files != null ? files : new File[0]).filter(file -> !file.isDirectory()).forEach(file -> {
                if (isJar(file)) {
                    try (JarFile jar = loadJar(file)) {
                        Enumeration<JarEntry> entries = jar.entries();

                        while (entries.hasMoreElements()) {
                            JarEntry jarEntry = entries.nextElement();

                            if (!jarEntry.isDirectory()) {
                                try {
                                    if (isClass(jarEntry)) {
                                        Class<?> clazz = Class.forName(className(jarEntry));
                                        if (loadExpansion(clazz)) break;

                                    }
                                } catch (Throwable ex) {
                                    String error = "Failed to load class '" + className(jarEntry) + "'";
                                    Util.print(error);
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        String error = "Failed to install expansion from JAR '" + file + "'";
                        Util.print(error);
                        throwable.printStackTrace();
                    }
                } else {
                    String error = "Found non-jar file at '" + file + "'. Please remove it!";
                    Util.print(error);
                }
            });

            int count = expansionsAmount();
            String msg = WordUtil.withAmount("Loaded %1s expansion", count);
            Util.print(msg);
        } catch (Throwable ex) {
            String error1 = "There was an extreme error loading any expansions for CombatLogX!";
            String error2 = "Please send this message to SirBlobman on SpigotMC along with your 'latest.log' file!";
            Util.print(error1, error2);
            ex.printStackTrace();
        }
    }
    
    public static void reloadConfigs() {
        for(CLXExpansion ex : EXPANSIONS.values()) {
            ex.onConfigReload();
        }
    }

    public static int expansionsAmount() {
        return EXPANSIONS.size();
    }

    public static CLXExpansion getByName(String name) {
        return EXPANSIONS.getOrDefault(name, null);
    }

    public static boolean isEnabled(String name) {
        return EXPANSIONS.containsKey(name);
    }

    public static String fileExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        int s1 = name.lastIndexOf('/'), s2 = name.lastIndexOf('\\');
        int s = Math.max(s1, s2);
        if (i > s) {
            int j = (i + 1);
            String ex = name.substring(j);
            return ex.toLowerCase();
        } else
            return "";
    }

    private static boolean isJar(File file) {
        String ext = fileExtension(file);
        return ext.equals("jar");
    }

    @SuppressWarnings("resource")
    private static synchronized JarFile loadJar(File file) throws Throwable {
        JarFile jar = new JarFile(file);
        ClassLoader cl = CombatLogX.CLASS_LOADER;
        URLClassLoader ucl = (URLClassLoader) cl;
        URI uri = file.toURI();
        URL url = uri.toURL();
        for (URL it : ucl.getURLs()) {
            if (it.equals(url)) {
                return jar;
            }
        }

        Class<?> urlc = URLClassLoader.class;
        Method addURL = urlc.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(ucl, url);
        return jar;
    }

    private static boolean isClass(JarEntry je) {
        String name = je.getName();
        return name.endsWith(".class");
    }

    private static String className(JarEntry je) {
        if (isClass(je)) {
            String name = je.getName();
            String r1 = name.replace(".class", "");
            String r2 = r1.replace("/", ".");
            return r2.replace(File.separator, ".");
        } else
            return "";
    }
}