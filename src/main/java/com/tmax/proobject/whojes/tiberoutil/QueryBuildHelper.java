package com.tmax.proobject.whojes.tiberoutil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.CaseFormat;
import com.google.gson.annotations.Expose;
import com.tmax.proobject.model.dataobject.DataObject;

class QueryBuildHelper {
	static void addConditionIsNull(String key, boolean and, StringBuilder sb) {
		sb.append(String.format(" %s is null", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)));
		return;
	}

	static void addConditionIsNotNull(String key, boolean and, StringBuilder sb) {
		sb.append(String.format(" %s is not null", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)));
		return;
	}

	static void addCondition(String key, @NotNull Object value, boolean and, StringBuilder sb) {
		sb.append(String.format(" %s = ?", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)));
		return;
	}

	static List<String> designateDefaultFields(Class<? extends DataObject> dataobjectclazz) {
		List<String> list = Arrays.asList(dataobjectclazz.getDeclaredFields()).stream().filter(field -> {
			return field.isAnnotationPresent(Expose.class);
		}).map(field -> field.getName()).collect(Collectors.toList());
		return list;
	}
	
	static Map<String, Method> fieldGetMethodFromDataObject(List<String> fieldList,
			Class<? extends DataObject> clazz) throws NoSuchMethodException, SecurityException {
		Map<String, Method> map = new HashMap<>();
		for (String field : fieldList) {
			Method method = clazz
					.getDeclaredMethod("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field));
			map.put(field, method);
		}
		return map;
	}

	static void prepareConditionStringBuilder(StringBuilder sb, boolean and) {
		if (sb.length() == 0) {
			sb.append(" WHERE");
		} else {
			sb.append(and ? " AND" : " OR");
		}
	}
}
