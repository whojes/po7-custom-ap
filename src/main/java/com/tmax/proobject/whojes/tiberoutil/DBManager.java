package com.tmax.proobject.whojes.tiberoutil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.tmax.proobject.dataobject.session.DBSession;
import com.tmax.proobject.dataobject.session.SessionContainer;
import com.tmax.proobject.model.dataobject.DataObject;
import com.tmax.proobject.whojes.exception.CkException;
import com.tmax.proobject.whojes.exception.CommonExceptionCode;

public class DBManager<TargetObject extends DataObject> {
	private static String datasource_;

	private Class<TargetObject> targetClazz_;
	private DBSession session_;
	private Connection conn_;

	private String tableName_;

	public DBManager() throws SQLException, CkException {
		init();
	}

	public DBManager(@NotNull Class<TargetObject> clazz) throws SQLException, CkException {
		targetClazz_ = Objects.requireNonNull(clazz);
		init();
	}

	public DBManager(@NotNull Class<TargetObject> clazz, String tableName) throws SQLException, CkException {
		this(clazz);
		tableName_ = tableName;
	}

	public void init() throws SQLException, CkException {
		if (datasource_ == null) {
			throw new CkException(CommonExceptionCode.DATASOURCE_MUST_BE_INITIALIZED, 500);
		}
		if (session_ == null || session_.isClosed()) {
			session_ = (DBSession) SessionContainer.getInstance().getSession(datasource_);
		}
		if (conn_ == null || conn_.isClosed()) {
			conn_ = session_.getConnection();
		}
	}

	public static void setDatasource(String datasource) {
		datasource_ = datasource;
	}

	public SelectQueryExecutor<TargetObject> useSelect() {
		return new SelectQueryExecutor<TargetObject>(conn_, Objects.requireNonNull(tableName_), Objects.requireNonNull(targetClazz_));
	}

	public InsertQueryExecutor<TargetObject> useInsert() {
		return new InsertQueryExecutor<TargetObject>(conn_, Objects.requireNonNull(tableName_), Objects.requireNonNull(targetClazz_));
	}

	public DeleteQueryExecutor<TargetObject> useDelete() {
		return new DeleteQueryExecutor<TargetObject>(conn_, Objects.requireNonNull(tableName_), Objects.requireNonNull(targetClazz_));
	}

	public UpdateQueryExecutor<TargetObject> useUpdate() {
		return new UpdateQueryExecutor<TargetObject>(conn_, Objects.requireNonNull(tableName_), Objects.requireNonNull(targetClazz_));
	}

	public Object useCustom(CheckedFunction<Connection, Object> f) throws Throwable {
		return f.run(conn_);
	}

	public void commit() throws SQLException {
		conn_.commit();
	}

	public void rollback() throws SQLException {
		conn_.rollback();
	}

	public DBManager<TargetObject> setTableName(String tableName) {
		this.tableName_ = tableName;
		return this;
	}

	public String getTableName() {
		return this.tableName_;
	}

}