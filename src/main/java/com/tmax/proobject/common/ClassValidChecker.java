package com.tmax.proobject.common;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

/**
 * ClassValidChecker
 */
public class ClassValidChecker {

    /**
     * this methods checks whether the annotated class inherits DataObject of
     * ProObject7 or not
     */
    protected static boolean isValidDataobjectClass(TypeElement te, Messager messager, Elements elementUtils) {
        if (te.getSuperclass().getKind() == TypeKind.NONE) { // java.lang.Object has superclass with NONE
            messager.printMessage(Kind.ERROR, "The Class must inherit the DataObject class.");
            return false;
        }

        TypeElement upperClass = elementUtils.getTypeElement(te.getSuperclass().toString());
        String upperClassName = upperClass.getQualifiedName().toString();

        if (!StaticClasses.DATAOBJECT_CLASS_NAME.equals(upperClassName)) {
            return isValidDataobjectClass(upperClass, messager, elementUtils);
        }

        return true;
    }

    protected static boolean isValidServiceObjectClass(TypeElement te, Messager messager, Elements elementUtils) {
        List<String> heritances = getUpperClassList(te);
        for (String upper : heritances) {
            upper = upper.substring(0, upper.indexOf("<"));
            if (StaticClasses.CK_SERVICEOBJECT_CLASS_NAME.equals(upper) || StaticClasses.SERVICEOBJECT_CLASS_NAME.equals(upper)) {
                return true;
            }
        }
        if (heritances.size() == 0) {
            messager.printMessage(Kind.ERROR,
                String.format("The Class %s Maybe inherit the Raw Type of CkServiceObject or ServiceObject class.",
                        te.getQualifiedName().toString()));
        }
        messager.printMessage(Kind.ERROR,
                String.format("The Class %s must inherit the CkServiceObject or ServiceObject class.",
                        te.getQualifiedName().toString()));
        return false;
    }

    protected static String getValidSuperClassFullname(TypeElement te, Messager messager, Elements elementUtils) {
        List<String> heritances = getUpperClassList(te);
        for (String upper: heritances) {
            if (upper.contains(StaticClasses.CK_SERVICEOBJECT_CLASS_NAME) || upper.contains(StaticClasses.SERVICEOBJECT_CLASS_NAME)) {
                return upper;
            }
        }
        return null;
    }

    private static List<String> getUpperClassList(TypeElement te) {
        List<String> interfaces = te.getInterfaces().stream().map(typeMirror -> typeMirror.toString())
                .collect(Collectors.toList());
        String superClassName = te.getSuperclass().toString();

        return Stream.concat(interfaces.stream(), Stream.of(superClassName))
                .filter(val -> val.contains("<")) // raw type not working
                .collect(Collectors.toList());
    }
}