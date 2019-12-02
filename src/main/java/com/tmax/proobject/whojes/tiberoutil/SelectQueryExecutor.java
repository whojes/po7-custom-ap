package com.tmax.proobject.whojes.tiberoutil;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonObject;
import com.tmax.proobject.model.dataobject.DataObject;
import com.tmax.proobject.model.network.RequestContext;

import org.jetbrains.annotations.NotNull;

public class SelectQueryExecutor<TargetObject extends DataObject> extends QueryExecutor<List<TargetObject>> {
	private StringBuilder conditionSb_ = new StringBuilder();
	private List<String> selectFields_;
	private boolean forUpdate = false;

	// for list query
	private String countQuery = null;
	private int total_;

	protected SelectQueryExecutor(Connection conn, String tableName, Class<TargetObject> clazz) {
		dataobjectclazz_ = clazz;
		conn_ = conn;
		basicQuery_ = "SELECT %s FROM " + tableName;
		objListForQueryPrepare_ = new ArrayList<>();
	}

	public int getTotal() {
		return this.total_;
	}

	public SelectQueryExecutor<TargetObject> selectFields(String... values) {
		selectFields_ = Arrays.asList(values);
		return this;
	}

	public SelectQueryExecutor<TargetObject> selectFields(List<String> values) {
		selectFields_ = values;
		return this;
	}

	public SelectQueryExecutor<TargetObject> selectForUpdate() {
		forUpdate = true;
		return this;
	}

	public SelectQueryExecutor<TargetObject> addConditionIsNull(String key) {
		return addConditionIsNull(key, true);
	}

	public SelectQueryExecutor<TargetObject> addConditionIsNotNull(String key) {
		return addConditionIsNotNull(key, true);
	}

	public SelectQueryExecutor<TargetObject> addCondition(String key, Object value) {
		return addCondition(key, value, true);
	}

	// Only AND operator
	public SelectQueryExecutor<TargetObject> addCondition(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			addCondition(entry.getKey(), entry.getValue(), true);
		}
		return this;
	}

	public SelectQueryExecutor<TargetObject> addConditionIsNull(String key, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addConditionIsNull(key, and, conditionSb_);
		return this;
	}

	public SelectQueryExecutor<TargetObject> addConditionIsNotNull(String key, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addConditionIsNotNull(key, and, conditionSb_);
		return this;
	}

	public SelectQueryExecutor<TargetObject> addCondition(String key, @NotNull Object value, boolean and) {
		QueryBuildHelper.prepareConditionStringBuilder(conditionSb_, and);
		QueryBuildHelper.addCondition(key, value, and, conditionSb_);
		objListForQueryPrepare_.add(value);
		return this;
	}

	public QueryExecutor<List<TargetObject>> buildListQuery(RequestContext rc) throws Throwable {
		String fields = "*";
		if (selectFields_ != null && selectFields_.size() != 0) {
			fields = selectFields_.stream().map(field -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field))
					.collect(Collectors.joining(", "));
		}
		unpreparedQuery_ = String.format(basicQuery_, fields);
		ListQueryGenerator lqg = new ListQueryGenerator(rc, unpreparedQuery_);
		ps_ = conn_.prepareStatement(lqg.buildQuery());
		countQuery = lqg.buildCountQuery("TOTAL");
		return this;
	}

	@Override
	public QueryExecutor<List<TargetObject>> buildQuery() throws Throwable {
		String fields = "*";
		if (selectFields_ != null && selectFields_.size() != 0) {
			fields = selectFields_.stream().map(field -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field))
					.collect(Collectors.joining(", "));
		}
		unpreparedQuery_ = String.format(basicQuery_, fields) + conditionSb_.toString()
				+ (forUpdate ? " FOR UPDATE" : "");

		ps_ = conn_.prepareStatement(unpreparedQuery_);
		prepare();

		return this;
	}

	@Override
	public List<TargetObject> execute() throws Throwable {
		Objects.requireNonNull(ps_);

		ResultSet rs = ps_.executeQuery();
		ResultSetMetaData rsmd = rs.getMetaData();
		int sizeOfColumn = rsmd.getColumnCount();

		List<TargetObject> list = new ArrayList<>();

		Class<?> msgJsonClass = Class.forName(dataobjectclazz_.getName() + "MsgJson");
		Method method = msgJsonClass.getMethod("unmarshal", byte[].class, int.class);

		while (rs.next()) {
			@SuppressWarnings("unchecked")
			JsonObject jo = new JsonObject();
			for (int index = 0; index < sizeOfColumn; index++) {
				String column = rsmd.getColumnName(index + 1);
				jo.addProperty(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column), rs.getString(column));
			}
			@SuppressWarnings("unchecked")
			TargetObject to = (TargetObject) method.invoke(msgJsonClass.newInstance(), jo.toString().getBytes(), 1);
			list.add(to);
		}

		if (countQuery != null) {
			ResultSet countResultSet = ps_.executeQuery(countQuery);
			while (countResultSet.next()) {
				total_ = countResultSet.getInt("TOTAL");
			}
		}
		return list;
	}
}
