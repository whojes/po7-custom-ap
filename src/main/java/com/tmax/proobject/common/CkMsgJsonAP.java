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

@SupportedAnnotationTypes({ "com.tmax.proobject.common.CkMsgJson" })
public class CkMsgJsonAP extends AbstractProcessor {
    private final static String SUFFIX = "MsgJson";

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment env) {
        super.init(env);
        this.elementUtils = env.getElementUtils();
        this.filer = env.getFiler();
        this.messager = env.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Set<? extends Element> elementList = roundEnv.getElementsAnnotatedWith(CkMsgJson.class);
        for (Element e : elementList) {
            if (!e.getKind().equals(ElementKind.CLASS)) {
                messager.printMessage(Kind.ERROR,
                        String.format("Only Class will be used for @%s", CkMsgJson.class.getSimpleName()));
                return true;
            }
            
            final TypeElement te = (TypeElement) e;
            if (!ClassValidChecker.isValidDataobjectClass(te, messager, elementUtils)) {
                return true;
            }

            MsgJsonFile msgJsonFile = new MsgJsonFile(te);
            try {
                makeSourceFile(msgJsonFile);
            } catch (IOException ioe) {
                messager.printMessage(Kind.ERROR, "File IO Failed: ".concat(ioe.getMessage()));
                return true;
            }
        }

        return true;
    }

    private void makeSourceFile(MsgJsonFile msgJsonFile) throws IOException {
        String fileName = msgJsonFile.getFullName() + SUFFIX;
        JavaFileObject jfo = filer.createSourceFile(fileName);
        try (Writer writer = jfo.openWriter()) {
            writer.write(msgJsonFile.getSourceCode());
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    private class MsgJsonFile {
        private final String sourceBeforeFormat = "package %s;\n"
                + "public class %s extends %s<%s> {}";
        private String fullName;
        private String packageName;
        private String className;
        private String sourceCode;

        public MsgJsonFile(TypeElement te) {
            packageName = elementUtils.getPackageOf(te).toString();
            className = te.getSimpleName().toString();
            fullName = te.getQualifiedName().toString();
            sourceCode = String.format(sourceBeforeFormat, packageName, className + SUFFIX, StaticClasses.CK_GSON_MESSAGE, className);
        }

        public String getSourceCode() {
            return this.sourceCode;
        }

        public String getFullName() {
            return this.fullName;
        }
    }
}
