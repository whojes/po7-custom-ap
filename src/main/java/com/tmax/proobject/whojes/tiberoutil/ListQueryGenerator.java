package com.tmax.proobject.whojes.tiberoutil;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.CaseFormat;
import com.tmax.proobject.model.network.ProObjectRequest;
import com.tmax.proobject.model.network.RequestContext;

@SuppressWarnings("ALL")
public class ListQueryGenerator {
    private String dbQuery;

    private String resultQuery;

    private static ArrayList<String> reserved = new ArrayList<>();

    private int offset = 0;
    private int limit = 0;
    private String sort = "";
    private String filter = "";
    private String fields = "";

    private String QprefixFixed = "select * from ";
    private String Qprefix = "select %s from ";

    private String QrnumFrontPart = "( select ROWNUM as rnum, il.* from ";
    private String QrnumRearPart = " il where ROWNUM <= %d ) where rnum >= %d ";

    private String QnoRnumFrontPart = " ( ";
    private String QnoRunmRearPart = " ) where ROWNUM >= %d ";

    private String QcountFrontPart = "select count(*) %s from ( ";
    private String QcountAliasPart = " as ";
    private String QcountRearPart = " )";

    static {
        reserved.add("offset");
        reserved.add("limit");
        reserved.add("fields");
        reserved.add("sort");
        reserved.add("q");
    }

    public ListQueryGenerator(RequestContext requestContext, String dbQuery) {
        this.dbQuery = dbQuery;

        restQueryParser(requestContext);
    }

    public ListQueryGenerator(RequestContext requestContext, String dbQuery, Map<String, String> aliasMap) {
        this(requestContext, dbQuery);

        aliasingValue(aliasMap);
    }

    private static String getQueryStringValue(RequestContext requestContext, String key) {
        ProObjectRequest requestMessage = requestContext.getRequest();
        String array[] = requestMessage.getQueryString(key);
        if (array == null) {
            return null;
        } else {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, array[0]);
        }
    }

    private void restQueryParser(RequestContext requestContext) {
        String result = getQueryStringValue(requestContext, "offset");
        this.offset = (result != null) ? Integer.parseInt(result) : 0;

        result = getQueryStringValue(requestContext, "limit");
        this.limit = (result != null) ? Integer.parseInt(result) : 0;

        result = getQueryStringValue(requestContext, "fields");
        this.fields = (result != null) ? result : "";

        result = getQueryStringValue(requestContext, "sort");
        this.sort = (result != null) ? result : "";

        result = getQueryStringValue(requestContext, "q");
        this.filter = (result != null) ? result : "";
    }

    private void aliasingValue(Map<String, String> aliasMap) {
        for (Entry<String, String> entry : aliasMap.entrySet()) {
            this.sort = this.sort.replace(entry.getKey(), entry.getValue());
            this.filter = this.filter.replace(entry.getKey(), entry.getValue());
            this.fields = this.fields.replace(entry.getKey(), entry.getValue());
        }
    }

    public String buildQuery() {
        this.resultQuery = "(" + this.QprefixFixed + "(" + this.dbQuery + ")" + parseCondition() + parseOrder() + ")";
        this.resultQuery = parseFields() + parsePaging();

        return this.resultQuery;
    }

    public String buildCountQuery() {
        String genQuery = "(" + this.QprefixFixed + "(" + this.dbQuery + ")" + parseCondition() + parseOrder() + ")";
        return String.format(this.QcountFrontPart, "") + genQuery + this.QcountRearPart;
    }

    public String buildCountQuery(String alias) {
        String genQuery = "(" + this.QprefixFixed + "(" + this.dbQuery + ")" + parseCondition() + parseOrder() + ")";
        return String.format(this.QcountFrontPart, this.QcountAliasPart + alias) + genQuery + this.QcountRearPart;
    }

    private String parseFields() {
        if (this.fields.equals(""))
            return String.format(Qprefix, "*");
        else
            return String.format(Qprefix, this.fields);
    }

    private String parsePaging() {
        if (this.limit <= 0 && this.offset <= 0)
            return this.resultQuery;

        if (this.offset <= 0) {
            int row_start = 1;
            int row_end = this.limit;
            return QrnumFrontPart + this.resultQuery + String.format(QrnumRearPart, row_end, row_start);
        }

        if (this.limit <= 0) {
            int row_start = this.offset;
            return QnoRnumFrontPart + this.resultQuery + String.format(QnoRunmRearPart, row_start);
        }

        int row_start = this.offset;
        int row_end = this.offset + this.limit - 1;
        return QrnumFrontPart + this.resultQuery + String.format(QrnumRearPart, row_end, row_start);

    }

    private String parseCondition() {
        if (filter.equals(""))
            return "";

        String q = this.filter;
        String query = " WHERE ";

        while (q.length() > 0) {
            int mid = indexNext(q);

            String token = "";
            char connector = 0;
            if (mid == 0)
                break; // Syntax Error
            else if (mid > 0) {
                token = q.substring(0, mid);
                connector = q.charAt(mid);
                q = q.substring(mid + 1);
            } else {// mid < 0
                token = new String(q);
                q = "";
            }

            query += tokenConverter(token);
            if (connector == '&')
                query += " AND ";
            else if (connector == '|')
                query += " OR ";
        }

        return query;
    }

    private String tokenConverter(String token) {
        String result = "";
        if (token.contains(">=")) {
            return token;
        }

        if (token.contains("<=")) {
            return token;
        }

        if (token.contains(">")) {
            return token;
        }

        if (token.contains("<")) {
            return token;
        }

        if (token.contains("!=")) {
            return token;
        }

        String[] KVArr = token.split("=");
        String K = KVArr[0];
        String V = KVArr[1];

        result = "lower(" + K + ") LIKE lower('%" + V + "%')";

        return result;
    }

    private String parseOrder() {
        String query = " ORDER BY ";
        if (this.sort.equals(""))
            return "";

        String[] tokens = this.sort.split(",");
        for (String token : tokens) {
            boolean isAscending = true;
            if (token.charAt(0) == '-') {
                isAscending = false;
                token = token.substring(1);
            }

            query += token + " " + (isAscending ? "ASC" : "DESC") + ",";
        }

        if (query.charAt(query.length() - 1) == ',')
            query = query.substring(0, query.length() - 1);
        return query;
    }

    // UTILITY
    private int indexNext(String str) {
        int inda = str.indexOf("&");
        int indo = str.indexOf("|");

        if (inda < 0 && indo < 0)
            return -1;
        if (inda < 0 && indo > 0)
            return indo;
        if (inda > 0 && indo < 0)
            return inda;

        if (inda < indo)
            return inda;
        return indo;
    }

}
