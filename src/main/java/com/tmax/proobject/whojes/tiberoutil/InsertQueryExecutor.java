package com.tmax.proobject.whojes.tiberoutil;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;
import com.tmax.proobject.model.dataobject.DataObject;
import com.tmax.proobject.whojes.exception.CkException;
import com.tmax.proobject.whojes.exception.CommonExceptionCode;

public class InsertQueryExecutor<TargetObject extends DataObject> extends QueryExecutor<Integer> {
	private List<TargetObject> insertList_ = new ArrayList<>();
	private List<String> fieldList_;
	private List<String> notInsertField_;

	protected InsertQueryExecutor(Connection conn, String tableName, Class<TargetObject> clazz) {
		dataobjectclazz_ = clazz;
		conn_ = conn;
		basicQuery_ = "INSERT INTO " + tableName + " (%s) VALUES";
	}

	public InsertQueryExecutor<TargetObject> addToInsert(TargetObject... to) {
		return addToInsert(Arrays.asList(to));
	}

	public InsertQueryExecutor<TargetObject> addToInsert(List<TargetObject> toList) {
		for (TargetObject to : toList) {
			insertList_.add(to);
		}
		return this;
	}

	public InsertQueryExecutor<TargetObject> insertFields(String... list) {
		return insertFields(Arrays.asList(list));
	}

	public InsertQueryExecutor<TargetObject> insertFields(List<String> list) {
		fieldList_ = list;
		return this;
	}
	
	public InsertQueryExecutor<TargetObject> notInsertFields(String... list) {
		return notInsertFields(Arrays.asList(list));
	}
	
	public InsertQueryExecutor<TargetObject> notInsertFields(List<String> list) {
		notInsertField_ = list;
		return this;
	}

	@Override
	public QueryExecutor<Integer> buildQuery() throws Throwable {
		if (insertList_.size() == 0) {
			throw new CkException(CommonExceptionCode.INTERNAL_SERVER_ERROR_EXCEPTION, "Not to Insert", 500);
		}

		// if fields not specified, all field in DataObject will be inserted.
		if (fieldList_ == null || fieldList_.size() == 0) {
			fieldList_ = QueryBuildHelper.designateDefaultFields(dataobjectclazz_);
		}
		
		if (notInsertField_ != null && notInsertField_.size() != 0) {
			for(String niField : notInsertField_) {
				fieldList_.remove(niField);
			}
		}

		if (fieldList_.size() == 0) {
			throw new CkException(CommonExceptionCode.INTERNAL_SERVER_ERROR_EXCEPTION, "Nothing to Insert", 500);
		}
		
		// prepare '?, ?, ?' in query
		String qms = null;
		for (int count = 0; count < fieldList_.size(); count++) {
			if(qms == null) {
				qms = "?";
			} else {
				qms += ",?";
			}
		}
		
		// prepare queries
		unpreparedQuery_ = String.format(basicQuery_,
				fieldList_.stream().map(field -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field))
						.collect(Collectors.joining(", "))) + String.format("(%s)", qms);
		
		ps_ = conn_.prepareStatement(unpreparedQuery_);
		
		// fieldName and method map
		Map<String, Method> getFieldMethodMap = QueryBuildHelper.fieldGetMethodFromDataObject(fieldList_, dataobjectclazz_);

		// add batch
		for (TargetObject to : insertList_) {
			objListForQueryPrepare_ = new ArrayList<>();
			for (String field : fieldList_) {
				getFieldMethodMap.get(field).invoke(to);
				objListForQueryPrepare_.add(getFieldMethodMap.get(field).invoke(to));
			}
			prepare();
			ps_.addBatch();
		}

		return this;
	}

	@Override
	public Integer execute() throws SQLException {
		Objects.requireNonNull(ps_);
		return ps_.executeBatch().length;
	}
}
