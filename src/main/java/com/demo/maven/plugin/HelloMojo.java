package com.demo.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//处理类还需要增加@Mojo注解，来标示其为一个Mojo类(即<plugin>中的<goal>)
@Mojo(name = "hello",requiresDependencyResolution = ResolutionScope.COMPILE)
//处理类需要继承 AbstractMojo，并实现execute()方法，execute()就是我们实际执行的操作
public class HelloMojo extends AbstractMojo {

    //@Parameter注解用来直接从pom文件中获取值，其本质就是我们插件的参数
    @Parameter(property = "greeting", defaultValue = "Hello World!")
    private String greeting;

    //也可以使用表达式来获取参数，如下是调用MavenSession.getCurrentProject()来获取当前工程
    //可用的表达式请参见：
    //https://maven.apache.org/ref/3.6.3/maven-core/apidocs/org/apache/maven/plugin/PluginParameterExpressionEvaluator.html
    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    //这里通过MavenProject获得当前工程的所有文件，然后将java文件和依赖输出
    public void execute() throws MojoExecutionException, MojoFailureException {
        Class<? extends HelloMojo> aClass = getClass();
        System.out.println(aClass);

        //getLog()用于返回一个类似于log4j的Logger，它可以用来创建debug/info/warn/error日志
        Log log = getLog();
        log.info("<==========河神说: Hello Mojo Start==========>" + greeting);

        //输出当前maven工程的依赖
        List dependencies = mavenProject.getDependencies();
        log.info("[Dependencies]");
        for (Object dependency : dependencies) {
            log.info("  - " + dependency.toString());
        }

        //输出当前maven工程的java文件
        log.info("[Files]");
        File basedir = mavenProject.getBasedir();
        List<File> files = searchFiles(basedir, "java");
        for (File file : files) {
            Date date = new Date(file.lastModified());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = dateFormat.format(date);
            log.info("  - " + file.getName() + ", lastModified=" + format);
        }
        log.info("<==========河神说: Hello Mojo End============>" + greeting);
    }

    public static List<File> searchFiles(File folder, final String keyword) {
        List<File> result = new ArrayList<File>();
        if (folder.isFile()){
            result.add(folder);
        }

        File[] subFolders = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                //接受文件名以keyword结尾的文件
                return file.getName().toLowerCase().endsWith(keyword);
            }
        });

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // 如果是文件则将文件添加到结果列表中
                    result.add(file);
                } else {
                    // 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
                    result.addAll(searchFiles(file, keyword));
                }
            }
        }

        return result;
    }
}