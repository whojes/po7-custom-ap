package com.tmax.proobject.common;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({ "com.tmax.proobject.common.CkServiceExecutor" })
public class CustomServiceExecutorAP extends AbstractProcessor {
    private final static String EXECUTOR_FILE_NAME = "com.tmax.proobject.engine.service.executor.ServiceExecutor";
    private final static String SUFFIX = "Executor";

    private Elements elementUtils;
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.elementUtils = env.getElementUtils();
        this.messager = env.getMessager();
        this.filer = env.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> elementList = roundEnv.getElementsAnnotatedWith(CkServiceExecutor.class);

        for (Element e : elementList) {
            if (!e.getKind().equals(ElementKind.CLASS)) {
                messager.printMessage(Kind.ERROR,
                        String.format("Only Class will be used for @%s", CkServiceExecutor.class.getSimpleName()));
                return true;
            }

            final TypeElement te = (TypeElement) e;
            if (!ClassValidChecker.isValidServiceObjectClass(te, messager, elementUtils)) {
                return true;
            }

            ExecutorFile executorFile = new ExecutorFile(te);

            try {
                makeSourceFile(executorFile);
            } catch (IOException ioe) {
                messager.printMessage(Kind.ERROR, "File IO Failed: ".concat(ioe.getMessage()));
                return true;
            }
        }
        return true;
    }

    private void makeSourceFile(ExecutorFile executorFile) throws IOException {
        String fileName = executorFile.getFullName() + SUFFIX;
        JavaFileObject jfo = filer.createSourceFile(fileName);
        try (Writer writer = jfo.openWriter()) {
            writer.write(executorFile.getSourceCode());
        }
    }

    private class ExecutorFile {
        private final String sourceBeforeFormat = "package %s;\n" 
            + "public class %s extends %s {\n"
            + "public %s() { this.serviceObject = new %s(); }\n" 
            + "@Override\n"
            + "public Object execute(Object arg0, String arg1) throws Throwable { return this.serviceObject.service(arg0); }\n"
            + "}";
        private String fullName;
        private String packageName;
        private String className;
        private String sourceCode;

        public ExecutorFile(TypeElement te) {
            packageName = elementUtils.getPackageOf(te).toString();
            className = te.getSimpleName().toString();
            fullName = te.getQualifiedName().toString();
            sourceCode = String.format(sourceBeforeFormat, packageName, className + SUFFIX, EXECUTOR_FILE_NAME,
                    className + SUFFIX, className);
        }

        public String getSourceCode() {
            return this.sourceCode;
        }

        public String getFullName() {
            return this.fullName;
        }
    }

}