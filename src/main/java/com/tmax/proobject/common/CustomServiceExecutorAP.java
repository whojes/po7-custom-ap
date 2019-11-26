package com.tmax.proobject.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({ "com.tmax.proobject.common.CkServiceExecutor" })
public class CustomServiceExecutorAP extends AbstractProcessor {
    private final String marking = "//generated";

    @Override
    public synchronized void init(ProcessingEnvironment env) {
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        String serviceClass = null;
        String servicePackage = null;
        File file = null;
        FileWriter fw = null;

        for (Element ae : roundEnv.getElementsAnnotatedWith(CkServiceExecutor.class)) {
            if (ae.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement te = (TypeElement) ae;

            String classFullname = te.getQualifiedName().toString();
            servicePackage = classFullname.substring(0, classFullname.lastIndexOf("."));
            serviceClass = classFullname.substring(classFullname.lastIndexOf(".") + 1);

            String sgName = servicePackage.split(System.getProperty("app_name") + ".")[1].split("\\.")[0];
            file = new File(System.getProperty("build_dir"), String.format("../../%s/src/main/java/%s/%sExecutor.java",
                    sgName, servicePackage.replaceAll("\\.", "/"), serviceClass));

            if (file.exists()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    String firstline = br.readLine();
                    br.close();
                    if (firstline.equals(marking)) {
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            } else {
                try {
                    fw = new FileWriter(file, false);
                    fw.write(getExecutorCode().replaceAll("packagename", servicePackage).replaceAll("classname",
                            serviceClass));
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String getExecutorCode() {
        return marking + "\npackage packagename;\n"
                + "import com.tmax.proobject.engine.service.executor.ServiceExecutor;\n"
                + "public class classnameExecutor extends ServiceExecutor {\n"
                + "public classnameExecutor() { this.serviceObject = new classname(); }\n" + "@Override\n"
                + "public Object execute(Object arg0, String arg1) throws Throwable { return this.serviceObject.service(arg0); }\n"
                + "}";
    }

}