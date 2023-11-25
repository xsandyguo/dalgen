${gg.setIgnoreOutput(sql.columnsCount <= 1 || sql.columnsInSameTable)}

<#include '/java_copyright.include'/>

package ${tableConfig.basepackage}.dalgen.operation.${tableConfig.className?lower_case};

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

<#include '/java_import.include'/>

/**
<#include '/java_description.include'/>
 */
public class ${sql.resultClassName} implements java.io.Serializable {
	private static final long serialVersionUID = -5216457518046898601L;
	
	<#list sql.columns as column>
	/** ${column.columnAlias!} */
	<#if column.possibleShortJavaType?ends_with('Money')>
	private Money ${column.columnNameFirstLower} = new Money(0,0);
	<#else>
	private ${column.possibleShortJavaType} ${column.columnNameFirstLower};
	</#if>
	</#list>

	<#list sql.columns as column>
	public void set${column.columnName}(${column.possibleShortJavaType} ${column.columnNameFirstLower}) {
		<#if column.possibleShortJavaType?ends_with('Money')>
		if(${column.columnNameFirstLower} == null) {
			this.${column.columnNameFirstLower} = new Money(0,0);
		}else {
			this.${column.columnNameFirstLower} = ${column.columnNameFirstLower};
		}		
		<#else>	
		this.${column.columnNameFirstLower} = ${column.columnNameFirstLower};
		</#if>
	}
	
	public ${column.possibleShortJavaType} get${column.columnName}() {
		return this.${column.columnNameFirstLower};
	}
	
	</#list>

    @Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
