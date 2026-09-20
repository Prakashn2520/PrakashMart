package com.prakash.prakashmart;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Embedded Apache Tomcat 9 runner for local testing, development, and visual inspection.
 * Serves the application at: http://localhost:8080/prakashmart
 */
public class TomcatServer {

    private static Tomcat tomcat;

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        start(port);
        tomcat.getServer().await();
    }

    public static void start(int port) throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Triggers default connector initialization

        File baseDir = new File("target/tomcat-embed");
        baseDir.mkdirs();
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        File docBase = new File("src/main/webapp");
        Context ctx = tomcat.addWebapp("/prakashmart", docBase.getAbsolutePath());

        // Attach compiled classes to /WEB-INF/classes for annotations and servlets
        StandardRoot resources = new StandardRoot(ctx);
        File classes = new File("target/classes");
        if (classes.exists()) {
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classes.getAbsolutePath(), "/"));
        }
        ctx.setResources(resources);
        ctx.addApplicationListener("com.prakash.prakashmart.listener.AppContextListener");

        System.out.println(">>> Apache Tomcat 9 running at: http://localhost:" + port + "/prakashmart");
        tomcat.start();
    }

    public static void stop() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
            tomcat = null;
        }
    }
}
