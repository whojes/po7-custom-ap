package com.tmax.proobject.common;

import java.io.File;
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

/**
 * ServiceGroup.xml Processing
 * 
 * @author whojes
 */
@SupportedAnnotationTypes({ "com.tmax.proobject.common.CkService" })
public class CustomServiceAP extends AbstractProcessor {
	private File configFile;
	private FileWriter fw;
	private String configFileName = "servicegroup.xml.generated";

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		// shit
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		String serviceName = null;
		String serviceClass = null;
		String inputClass = null;
		String outputClass = null;
		for (Element ae : roundEnv.getElementsAnnotatedWith(CkService.class)) {
			if (ae.getKind() != ElementKind.CLASS) {
				continue;
			}
			TypeElement te = (TypeElement) ae;
			serviceClass = te.getQualifiedName().toString();

			String[] temp = te.getSuperclass().toString().split("<")[1].split(">")[0].split(",");
			inputClass = temp[0];
			outputClass = temp[1];

			CkService cs = ae.getAnnotation(CkService.class);
			String s = cs.serviceName();
			String m = null;

			if (cs.method().equals(HttpMethod.CUSTOM)) {
				m = cs.customMethod();
			} else {
				m = cs.method().getValue();
			}

			serviceName = s.substring(0, 1).toUpperCase() + s.substring(1, s.length()) + m;

			String serviceXml = 
			"\t<ns17:service-object>\n" 
				+ "\t\t<ns17:name>" + serviceName + "</ns17:name>\n"
				+ "\t\t<ns17:class-name>" + serviceClass + "</ns17:class-name>\n" 
				+ "\t\t<ns17:input-dto>" + inputClass + "</ns17:input-dto>\n" 
				+ "\t\t<ns17:output-dto>" + outputClass + "</ns17:output-dto>\n"
				+ "\t\t<ns17:service-type>COMPLEX</ns17:service-type>\n" 
			+ "\t</ns17:service-object>\n\n";

			if (fw == null) {
				String sgName = serviceClass.split(System.getProperty("app_name")+".")[1].split("\\.")[0];
				String buildDir = System.getProperty("build_dir");
				createFileWriter(sgName, buildDir);
			}

			try {
				fw.write(serviceXml);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}


	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
	
	private void createFileWriter(String sgName, String buildDir) {
		File configDir = new File(buildDir, String.format("%s/%s/%s", "servicegroup", sgName, "config"));
		if (!configDir.exists()) {
			configDir.mkdir();
		}

		configFile = new File(configDir, configFileName);
		try {
			if (configFile.exists()) {
				fw = new FileWriter(configFile, true);
			} else {
				fw = new FileWriter(configFile, true);
				fw.write("\t<!-- generate -->\n");
				fw.flush();
			}
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("FileWriter Create Failed: " + configFileName);
		}
	}
}
