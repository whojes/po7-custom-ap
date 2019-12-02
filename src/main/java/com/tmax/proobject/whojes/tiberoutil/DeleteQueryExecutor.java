package com.tmax.proobject.whojes.tiberoutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

import com.tmax.proobject.whojes.exception.CkException;
import com.tmax.proobject.whojes.exception.CommonExceptionCode;
import com.tmax.proobject.model.dataobject.DataObject;

public class DeleteQueryExecutor<TargetObject extends DataObject> extends QueryExecutor<Integer> {

	private StringBuilder conditionSb_ = new StringBuilder();
	
	protected DeleteQueryExecutor(Connection conn, String tableName, Class<TargetObject> clazz) {
		dataobjectclazz_ = clazz;
		conn_ = conn;
		basicQuery_ = "DELETE FROM " + tableName;
	}

	public DeleteQueryExecutor<TargetObject> addConditionIsNull(String key) {
		return addConditionIsNull(key, true);
	}

	public DeleteQueryExecutor<TargetObject> addConditionIsNotNull(String key) {
		return addConditionIsNotNull(key, true);
	}

	public DeleteQueryExecutor<TargetObject> addCondition(String key, Object value) {
		return addCondition(key, value, true);
	}

	// Only AND operator
	public DeleteQueryExecutor<TargetObject> addCondition(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			addCondition(entry.getKey(), entry.getValue(), true);
		}
		return this;
	}

	public DeleteQueryExecutor<TargetObject> addConditionIsNull(String key, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addConditionIsNull(key, and, conditionSb_);
		return this;
	}

	public DeleteQueryExecutor<TargetObject> addConditionIsNotNull(String key, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addConditionIsNotNull(key, and, conditionSb_);
		return this;
	}

	public DeleteQueryExecutor<TargetObject> addCondition(String key, @NotNull Object value, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addCondition(key, value, and, conditionSb_);
		objListForQueryPrepare_.add(value);
		return this;
	}
	
	@Override
	public QueryExecutor<Integer> buildQuery() throws Throwable {
		if (conditionSb_.length() == 0) {
			throw new CkException(CommonExceptionCode.BAD_REQUEST_EXCEPTION, "DO NOT DELETE ALL ROWS AT ONCE", 400);
		}
		
		unpreparedQuery_ = basicQuery_ + conditionSb_.toString();
		
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
