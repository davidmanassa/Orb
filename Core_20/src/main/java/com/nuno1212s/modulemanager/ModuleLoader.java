package com.nuno1212s.modulemanager;

import lombok.Cleanup;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Loads a module
 */
public class ModuleLoader {

    File moduleFile;

    @Getter
    Module mainClass;

    public ModuleLoader(File moduleFile) {
        this.moduleFile = moduleFile;
    }

    public void load() {
        try (JarFile file = new JarFile(moduleFile)){
            ZipEntry entry = file.getEntry("moduleInfo.yml");
            if (entry == null) {
                System.out.println("Module Info is missing from module " + moduleFile.getName().replace(".jar", ""));
                return;
            }
            @Cleanup
            InputStream stream = file.getInputStream(entry);
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            getMainClass(yamlConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMainClass(Object j) {
        this.mainClass = (Module) j;
        ModuleData annotation = j.getClass().getAnnotation(ModuleData.class);
        mainClass.setModuleName(annotation.name());
        mainClass.setVersion(annotation.version());
        mainClass.setDependencies(annotation.dependencies());
    }

    public void getMainClass(YamlConfiguration yml) {
        String mainClassPath = yml.getString("MainClass");

        try (URLClassLoader loader = new URLClassLoader(new URL[]{moduleFile.toURL()}, this.getClass().getClassLoader())){
            Class toLoad = Class.forName(mainClassPath, true, loader);
            Object mainClass = toLoad.newInstance();
            setMainClass(mainClass);
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
