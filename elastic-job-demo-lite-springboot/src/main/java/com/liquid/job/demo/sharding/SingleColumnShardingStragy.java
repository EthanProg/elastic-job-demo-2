/**
 * 
 */
package com.liquid.job.demo.sharding;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.liquid.job.demo.sharding.orm.dao.SupplierInvoiceMapper;
import com.liquid.job.demo.sharding.util.SpringContext;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.api.strategy.JobShardingStrategy;
import io.elasticjob.lite.api.strategy.impl.AverageAllocationJobShardingStrategy;

/**
 * @author Liquid
 *
 */
@Component
@PropertySources({ @PropertySource("classpath:application.yml") })
public class SingleColumnShardingStragy implements JobShardingStrategy {
	
	@Value("${simpleJob.shardingTable}")
	private String shardingTable;
	
	@Value("${simpleJob.shardingColumn}")
	private String shardingColumn;
	
	@Autowired
	private SupplierInvoiceMapper mapper;

	private AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();
	
	private String shardingItemParameters;
	
	private int shardingTotalCount;
	
	@Override
	public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName,
			int shardingTotalCount) {
		// TODO Auto-generated method stub
		if(mapper == null){
			mapper = SpringContext.getBean(SupplierInvoiceMapper.class);
		}
		if(shardingTable == null || shardingColumn == null){
			Environment env = SpringContext.getApplicationContext().getEnvironment();
			shardingTable = env.getProperty("simpleJob.shardingTable");
			shardingColumn = env.getProperty("simpleJob.shardingColumn");
		}
		List<Map<String, String>> columnCountList = mapper.selectGroupCount(shardingTable, shardingColumn);
		this.shardingTotalCount = columnCountList.size();
		// 刷新分片参数
		StringBuffer shardingItemParametersBuffer = new StringBuffer();
		for(Map<String, String> columnMap : columnCountList){
			for(String key : columnMap.keySet()){
				if(key.equals(shardingColumn)){
					shardingItemParametersBuffer.append(",");
					shardingItemParametersBuffer.append(columnCountList.indexOf(columnMap));
					shardingItemParametersBuffer.append("=");
					shardingItemParametersBuffer.append(columnMap.get(shardingColumn));
				}
			}
		}
		shardingItemParameters = shardingItemParametersBuffer.length() > 0 ? shardingItemParametersBuffer.substring(1)
				: "";
		return averageAllocationJobShardingStrategy.sharding(jobInstances, jobName, this.shardingTotalCount);
	}

	public String getShardingItemParameters() {
		return shardingItemParameters;
	}

	public int getShardingTotalCount() {
		return shardingTotalCount;
	}

}
