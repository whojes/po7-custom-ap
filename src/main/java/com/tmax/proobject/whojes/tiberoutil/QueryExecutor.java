package com.tmax.proobject.whojes.tiberoutil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.tmax.proobject.logger.ProObjectLogger;
import com.tmax.proobject.logger.application.ServiceLogger;
import com.tmax.proobject.model.dataobject.DataObject;

public abstract class QueryExecutor<ExecuteOutType> {

	public Class<? extends DataObject> dataobjectclazz_;

	protected String basicQuery_;
	protected String unpreparedQuery_;
	protected Connection conn_;
	protected PreparedStatement ps_;
	protected List<Object> objListForQueryPrepare_ = new ArrayList<>();
	protected ProObjectLogger logger_ = ServiceLogger.getLogger(); 

	void prepare() throws SQLException {
		for (int index = 1; index < objListForQueryPrepare_.size() + 1; index++) {
			Object o = objListForQueryPrepare_.get(index - 1);
			if (o == null) {
				ps_.setString(index, "");
			} else if (o instanceof String) {
				ps_.setString(index, (String) o);
			} else if (o instanceof Integer) {
				ps_.setInt(index, (Integer) o);
			} else if (o instanceof Long) {
				ps_.setLong(index, (Long) o);
			} else if (o instanceof Date) {
				ps_.setDate(index, (Date) o);
			} else if (o instanceof Timestamp) {
				ps_.setTimestamp(index, (Timestamp) o);
			} else if (o instanceof Double) {
				ps_.setDouble(index, (Double) o);
			} else if (o instanceof Float) {
				ps_.setFloat(index, (Float) o);
			} else {
				ps_.setString(index, o.toString());
			}
		}
	}

	public String getBasicQuery() {
		return basicQuery_;
	}

	public String getUnpreparedQuery() {
		return unpreparedQuery_;
	}

	public Connection getConnection() {
		return conn_;
	}

	public PreparedStatement getPreparedStatement() {
		return ps_;
	}

	public QueryExecutor<ExecuteOutType> test(NoParamNoReturn r) throws Throwable {
		Objects.requireNonNull(unpreparedQuery_);
		r.run(this);
		return this;
	};

	public abstract QueryExecutor<ExecuteOutType> buildQuery() throws Throwable;

	public abstract ExecuteOutType execute() throws Throwable;
	
	public Object customExecute(CheckedFunction<PreparedStatement, Object> f) throws Throwable {
		Objects.requireNonNull(ps_);
		return f.run(ps_);
	};
}
