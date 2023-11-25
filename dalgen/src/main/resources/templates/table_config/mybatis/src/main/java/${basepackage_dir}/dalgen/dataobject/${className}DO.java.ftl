<#assign table = tableConfig.table>   
<#assign className = table.className>   
<#assign classNameLower = className?uncap_first> 
<#include '/java_copyright.include'/>

package ${tableConfig.basepackage}.dalgen.dataobject;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

<#include '/java_import.include'/>

/**
 * ${tableConfig.className}DO
<#include '/java_description.include'/>
 */
public class ${className}DO implements java.io.Serializable {
	private static final long serialVersionUID = -5216457518046898601L;
	
	<#list table.columns as column>
	/**
	 * ${column.columnAlias!} 		db_column: ${column.sqlName} 
	 */
	<#if column.simpleJavaType?ends_with('Money')>
	private Money ${column.columnNameFirstLower} = new Money(0,0);
	<#else>
	private ${column.simpleJavaType} ${column.columnNameFirstLower};
	</#if>
	</#list>

<@generateJavaColumns/>

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		<#list table.columns as column>
			.append("${column.columnName}",get${column.columnName}())
		</#list>
			.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		<#list table.pkColumns as column>
			.append(get${column.columnName}())
		</#list>
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {return false;}
		if(this == obj) {return true;}
		if(!(obj instanceof ${className}DO)) {return false;}
		${className}DO other = (${className}DO)obj;
		return new EqualsBuilder()
			<#list table.pkColumns as column>
			.append(get${column.columnName}(),other.get${column.columnName}())
			</#list>
			.isEquals();
	}
}

<#macro generateJavaColumns>
	<#list table.columns as column>
	
	public void set${column.columnName}(${column.simpleJavaType} ${column.columnNameFirstLower}) {
		<#if column.simpleJavaType?ends_with('Money')>
		if(${column.columnNameFirstLower} == null) {
			this.${column.columnNameFirstLower} = new Money(0,0);
		}else {
			this.${column.columnNameFirstLower} = ${column.columnNameFirstLower};
		}		
		<#else>
		this.${column.columnNameFirstLower} = ${column.columnNameFirstLower};
		</#if>
	}
	
	public ${column.simpleJavaType} get${column.columnName}() {
		return this.${column.columnNameFirstLower};
	}
	</#list>
</#macro>