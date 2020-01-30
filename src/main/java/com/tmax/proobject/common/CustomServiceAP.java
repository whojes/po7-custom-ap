package com.tmax.proobject.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
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

/**
 * ServiceGroup.xml Processing
 * 
 * @author whojes
 */
@SupportedAnnotationTypes({ "com.tmax.proobject.common.CkService" })
public class CustomServiceAP extends AbstractProcessor {
	private final static String CONFIG_FILE_NAME = "servicegroup.xml.generated";
	private final static String APP_NAME = System.getProperty("app_name");
	private final static String BUILD_DIR = System.getProperty("build_dir");

	private Elements elementUtils;
	private Messager messager;
	private Map<String, List<ServiceConfig>> configXmlMap = new HashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		this.elementUtils = env.getElementUtils();
		this.messager = env.getMessager();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CkService.class);

		for (Element e : elements) {
			if (!e.getKind().equals(ElementKind.CLASS)) {
				messager.printMessage(Kind.ERROR,
						String.format("Only Class will be used for @%s", CkService.class.getSimpleName()));
				return true;
			}
			final TypeElement te = (TypeElement) e;

			if (!ClassValidChecker.isValidServiceObjectClass(te, messager, elementUtils)) {
				return true;
			}

			final String upperClassFullname = ClassValidChecker.getValidSuperClassFullname(te, messager, elementUtils);
			final String serviceClass = te.getQualifiedName().toString();

			CkService annotation = e.getAnnotation(CkService.class);
			String s = annotation.serviceName();
			String m = annotation.method().equals(HttpMethod.CUSTOM) ? annotation.customMethod()
					: annotation.method().getValue();
			
			String serviceName = s.substring(0, 1).toUpperCase() + s.substring(1, s.length()) + m;

			ServiceConfig serviceConfig = new ServiceConfig(serviceName, serviceClass, upperClassFullname);

			String sgName = null;
			try {
				sgName = serviceClass.split(APP_NAME + ".")[1].split("\\.")[0];
			} catch (NullPointerException | ArrayIndexOutOfBoundsException exception) {
				messager.printMessage(Kind.ERROR, exception.toString());
				return true;
			}

			List<ServiceConfig> configList = configXmlMap.get(sgName);
			if (configList == null) {
				configList = new ArrayList<>();
				configXmlMap.put(sgName, configList);
			}
			configList.add(serviceConfig);
		}

		// Generate Config file
		for (Entry<String, List<ServiceConfig>> entry : configXmlMap.entrySet()) {
			try {
				createConfigFile(entry.getKey(), entry.getValue());
			} catch (IOException ioe) {
				messager.printMessage(Kind.ERROR, ioe.getMessage());
				return true;
			}
		}

		// for next round
		configXmlMap.clear();

		return true;
	}

	private void createConfigFile(String serviceGroupName, List<ServiceConfig> configList) throws IOException {
		File configDir = new File(BUILD_DIR, String.format("%s/%s/%s", "servicegroup", serviceGroupName, "config"));
		if (!configDir.exists()) {
			configDir.mkdir();
		}

		File configFile = new File(configDir, CONFIG_FILE_NAME);
		try (FileWriter fw = new FileWriter(configFile, true)) {
			fw.write("\t<!-- generate -->\n");
			for (ServiceConfig config : configList) {
				fw.write(config.getConfigXml());
			}
			fw.flush();
		}
	}

	private class ServiceConfig {
		private final String configXmlBeforFormatted = 
		"\t<ns17:service-object>\n" 
			+ "\t\t<ns17:name>%s</ns17:name>\n"
			+ "\t\t<ns17:class-name>%s</ns17:class-name>\n" 
			+ "\t\t<ns17:input-dto>%s</ns17:input-dto>\n"
			+ "\t\t<ns17:output-dto>%s</ns17:output-dto>\n" 
			+ "\t\t<ns17:service-type>COMPLEX</ns17:service-type>\n"
		+ "\t</ns17:service-object>\n\n";

		private String inputClass;
		private String outputClass;
		private String serviceClass;
		private String serviceName;
		private String configXml;

		public ServiceConfig(String serviceName, String className, String upperClassFullname) {
			int f = upperClassFullname.indexOf("<");
			int l = upperClassFullname.lastIndexOf(">");
			String[] temp = upperClassFullname.substring(f + 1, l).split(",");

			this.inputClass = temp[0].split("<")[0];
			this.outputClass = temp[1].split("<")[0];
			this.serviceName = serviceName;
			this.serviceClass = className;

			configXml = String.format(configXmlBeforFormatted, this.serviceName, this.serviceClass, this.inputClass,
					this.outputClass);
		}

		public String getConfigXml() {
			return this.configXml;
		}
	}

}
