<#include '/java_copyright.include'/>
package ${tableConfig.basepackage}.dalgen.daointerface;
import org.springframework.dao.DataAccessException;
import ${tableConfig.basepackage}.dalgen.operation.${tableConfig.className?lower_case}.*;
import ${tableConfig.basepackage}.dalgen.dataobject.*;

<#include '/java_import.include'/>

/**
 * ${tableConfig.className}DAO
<#include '/java_description.include'/>
 */
public interface ${tableConfig.className}DAO {

<#list tableConfig.sqls as sql>

	/**
	 * ${sql.remarks!}
	 * sql:
	 * <pre>${StringHelper.removeCrlf(sql.executeSql)?trim}</pre> 
	 */
	<@generateResultClassName sql 'DO'/> ${sql.operation}(<@generateOperationArguments sql/>) throws DataAccessException;
</#list>

}



