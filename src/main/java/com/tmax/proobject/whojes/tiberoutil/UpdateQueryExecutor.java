package com.tmax.proobject.whojes.tiberoutil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Streams;
import com.tmax.proobject.model.dataobject.DataObject;
import com.tmax.proobject.whojes.exception.CkException;
import com.tmax.proobject.whojes.exception.CommonExceptionCode;

public class UpdateQueryExecutor<TargetObject extends DataObject> extends QueryExecutor<Integer> {

	private StringBuilder conditionSb_ = new StringBuilder();
	private List<Object> objListForCondition_ = new ArrayList<>();

	private List<String> fieldList_;
	private List<Object> objListForField_ = new ArrayList<>();
	private List<String> notInsertField_;

	private TargetObject updateTarget;

	protected UpdateQueryExecutor(Connection conn, String tableName, Class<TargetObject> clazz) {
		dataobjectclazz_ = clazz;
		conn_ = conn;
		basicQuery_ = "UPDATE " + tableName + " SET ";
	}

	public TargetObject getUpdateTarget() {
		return updateTarget;
	}

	public UpdateQueryExecutor<TargetObject> setUpdateTarget(TargetObject updateTarget) {
		this.updateTarget = updateTarget;
		return this;
	}

	public UpdateQueryExecutor<TargetObject> updateFields(String... values) {
		fieldList_ = Arrays.asList(values);
		return this;
	}

	public UpdateQueryExecutor<TargetObject> updateFields(List<String> values) {
		fieldList_ = values;
		return this;
	}

	public UpdateQueryExecutor<TargetObject> notInsertFields(String... list) {
		return notInsertFields(Arrays.asList(list));
	}
	
	public UpdateQueryExecutor<TargetObject> notInsertFields(List<String> list) {
		notInsertField_ = list;
		return this;
	}

	public UpdateQueryExecutor<TargetObject> addConditionIsNull(String key) {
		return addConditionIsNull(key, true);
	}

	public UpdateQueryExecutor<TargetObject> addConditionIsNotNull(String key) {
		return addConditionIsNotNull(key, true);
	}

	public UpdateQueryExecutor<TargetObject> addCondition(String key, Object value) {
		return addCondition(key, value, true);
	}

	// Only AND operator
	public UpdateQueryExecutor<TargetObject> addCondition(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			addCondition(entry.getKey(), entry.getValue(), true);
		}
		return this;
	}

	public UpdateQueryExecutor<TargetObject> addConditionIsNull(String key, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addConditionIsNull(key, and, conditionSb_);
		return this;
	}

	public UpdateQueryExecutor<TargetObject> addConditionIsNotNull(String key, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addConditionIsNotNull(key, and, conditionSb_);
		return this;
	}

	public UpdateQueryExecutor<TargetObject> addCondition(String key, @NotNull Object value, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addCondition(key, value, and, conditionSb_);
		objListForCondition_.add(value);
		return this;
	}

	@Override
	public QueryExecutor<Integer> buildQuery() throws Throwable {
		Objects.requireNonNull(updateTarget);
		
		// field list 정리
		if (fieldList_ == null || fieldList_.isEmpty()) {
			fieldList_ = QueryBuildHelper.designateDefaultFields(dataobjectclazz_);
		}
		if (fieldList_ == null || fieldList_.isEmpty()) {
			throw new CkException(CommonExceptionCode.INTERNAL_SERVER_ERROR_EXCEPTION, "CANNOT UPDATE ANY FIELD", 500);
		}
		if (notInsertField_ != null && notInsertField_.size() != 0) {
			for(String niField : notInsertField_) {
				fieldList_.remove(niField);
			}
		}

		// update 문의 FIELD_NAME = ? 쿼리생성과 ? 에 들어갈 값을 찾기 위해 reflect를 이용해 method 객체를 가져와
		// 매핑
		Map<String, Method> getFieldMethodMap = QueryBuildHelper.fieldGetMethodFromDataObject(fieldList_,
				dataobjectclazz_);

		// update 할 field 이름마다 FIELD_NAME = ? 를 작성하고 ? 에 들어갈 값을 targetObject로부터 가져옴
		String fieldsQuery = fieldList_.stream().map(field -> {
			Object object = null;
			try {
				object = getFieldMethodMap.get(field).invoke(updateTarget);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger_.severe(e.toString());
			}
			objListForField_.add(object);
			return String.format(" %s = ?", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field));
		}).collect(Collectors.joining(", "));

		// object list concat
		objListForQueryPrepare_ = Streams.concat(objListForField_.stream(), objListForCondition_.stream())
				.collect(Collectors.toList());

		// prepare queries
		unpreparedQuery_ = basicQuery_ + fieldsQuery + conditionSb_.toString();

		ps_ = conn_.prepareStatement(unpreparedQuery_);
		prepare();

		return this;
	}

	@Override
	public Integer execute() throws Throwable {
		Objects.requireNonNull(ps_);
		return ps_.executeUpdate();
	}
}
