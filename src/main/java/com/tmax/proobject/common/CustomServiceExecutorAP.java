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
    private final String marking = System.getProperty("generate_mark") != null ? System.getProperty("generate_mark")
            : "//generated";
    private final String timestamp = "\n//" + System.currentTimeMillis();

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

            CkServiceExecutor cs = ae.getAnnotation(CkServiceExecutor.class);
            ExecutorType et = cs.executorType();

            String sgName = servicePackage.split(System.getProperty("app_name") + ".")[1].split("\\.")[0];
            file = new File(System.getProperty("build_dir"), String.format("../../%s/src/main/java/%s/%sExecutor.java",
                    sgName, servicePackage.replaceAll("\\.", "/"), serviceClass));

            if (file.exists()) {
                if (marking.equals(System.getProperty(file.toString()))) {
                    file.delete();
                    continue;
                }
                String firstLine = null;
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    firstLine = br.readLine();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (marking.equals(firstLine)) {
                    file.delete();
                } else {
                    continue;
                }
            }
            try {
                fw = new FileWriter(file, false);
                fw.write(getExecutorCode(et).replaceAll("packagename", servicePackage).replaceAll("classname",
                        serviceClass));
                fw.flush();
                fw.close();
                System.setProperty(file.toString(), marking);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String getExecutorCode(ExecutorType et) {
        switch (et) {
        case DEFAULT:
            return marking + timestamp + "\npackage packagename;\n"
                    + "import com.tmax.proobject.engine.service.executor.ServiceExecutor;\n"
                    + "public class classnameExecutor extends ServiceExecutor {\n"
                    + "public classnameExecutor() { this.serviceObject = new classname(); }\n" + "@Override\n"
                    + "public Object execute(Object arg0, String arg1) throws Throwable { return this.serviceObject.service(arg0); }\n"
                    + "}";
        default:
            return "";
        }
    }
}