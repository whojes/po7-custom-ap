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

@SupportedAnnotationTypes({ "com.tmax.proobject.common.CkMsgJson" })
public class CkMsgJsonAP extends AbstractProcessor {
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

        for (Element ae : roundEnv.getElementsAnnotatedWith(CkMsgJson.class)) {
            if (ae.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement te = (TypeElement) ae;

            String classFullname = te.getQualifiedName().toString();
            servicePackage = classFullname.substring(0, classFullname.lastIndexOf("."));
            serviceClass = classFullname.substring(classFullname.lastIndexOf(".") + 1);

            file = new File(System.getProperty("build_dir"), String.format("../../src/main/java/%s/%sMsgJson.java",
                    servicePackage.replaceAll("\\.", "/"), serviceClass));
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
                fw.write(getMsgJsonCode().replaceAll("packagename", servicePackage).replaceAll("classname",
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

    private String getMsgJsonCode() {
        return marking + timestamp + "\npackage packagename;\n"
        // TODO: import class must be changed...
        // + "import com.tmax.proobject.common.GsonMessage;\n"
                + "import com.tmax.appcenter.dataobject.common.GsonMessage;\n"
                + "public class classnameMsgJson extends GsonMessage<classname> {}";
    }
}
