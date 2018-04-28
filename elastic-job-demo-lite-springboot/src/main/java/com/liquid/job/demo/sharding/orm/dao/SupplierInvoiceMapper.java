package com.liquid.job.demo.sharding.orm.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SupplierInvoiceMapper {

	@Select({ "select ${column}, count(${column}) as site_count FROM ${tableName} group by ${column}" })
	List<Map<String, String>> selectGroupCount(@Param("tableName") String tableName, @Param("column") String column);
}