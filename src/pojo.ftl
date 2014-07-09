package ${package};

<#list imports as i>
import ${i};
</#list>

/**
this is a wrapper classes generated from ${sourceClass}
*/

public class ${className} <#if super??>extends ${super}</#if>{
<#list getters as g>
  private ${g.retType} ${g.internalRefFieldName};
</#list>

  public ${className}(${sourceClass} val){
<#if super??>
    super(val);
</#if>
<#list getters as g>
<#if g.isCollection>
    this.${g.internalRefFieldName} = new ${g.collectionImplType}<>();
<#if g.collectionImplType == 'ArrayList' || g.collectionImplType == 'HashSet'>
    for (${g.sourceClass} v : refVal.${g.retMethod}()){
    <#if g.genericTypeIsGenerated>
      this.${g.internalRefFieldName}.add(new ${g.genericTypes[0]}(v));
    <#else>
      this.${g.internalRefFieldName}.add(v);
    </#if>
    }
<#else>
    for (Map.Entry<${g.mapKeyTypeSourceFullName}, ${g.mapValueTypeSourceFullName}> entry : refVal.${g.retMethod}().entrySet()) {
        <#if g.mapKeyTypeIsGenerated>
        ${g.mapKeyType} key = new ${g.mapKeyType}(entry.getKey());
        <#else>
        ${g.mapKeyType} key = entry.getKey();
        </#if>
        <#if g.mapValueTypeIsGenerated>
        ${g.mapValueType} value = new ${g.mapValueType}(entry.getValue());
        <#else>
        ${g.mapValueType} value = entry.getValue();
        </#if>
        this.${g.internalRefFieldName}.put(key, value);
    }
</#if>
<#elseif g.genericTypeIsGenerated>
        this.${g.internalRefFieldName} = new ${g.retType}(val.${g.retMethod}());
<#else>
        this.${g.internalRefFieldName} = val.${g.retMethod}();
</#if>
</#list>
  }

<#list getters as g>
  public ${g.retType} ${g.methodName}(){
    return this.${g.internalRefFieldName};
  }

  public void ${g.setterName}(${g.retType} val){
    this.${g.internalRefFieldName} = val;
  }
</#list>

  protected void initSchema(${sourceClass} valToInit){
<#list getters as g>
<#if g.isCollection>
    <#if g.collectionImplType == 'ArrayList'>
    List<${g.sourceClass}> ${g.fieldName} = new ArrayList<>();
    <#if g.genericTypeIsGenerated>
    for(${g.genericTypes[0]} p : this.${g.internalRefFieldName}){
    ${g.fieldName}.add(p.toSchema());
    }
    <#else>
    for(${g.sourceClass} p : this.${g.internalRefFieldName}){
    ${g.fieldName}.add(p);
    }
    </#if>
    valToInit.${g.setterName}(${g.fieldName});
    <#elseif g.collectionImplType == 'HashSet'>
    Set<${g.sourceClass}> ${g.fieldName} = new HashSet<>();
    <#if g.genericTypeIsGenerated>
    for(${g.genericTypes[0]} p : this.${g.internalRefFieldName}){
    ${g.fieldName}.add(p.toSchema());
    }
    <#else>
    for(${g.sourceClass} p : this.${g.internalRefFieldName}){
    ${g.fieldName}.add(p);
    }
    </#if>
    valToInit.${g.setterName}(${g.fieldName});
    <#else>
    <#--init map fields-->
    Map<${g.mapKeyTypeSourceFullName}, ${g.mapValueTypeSourceFullName}> ${g.fieldName} = new HashMap<>();
    for(Map.Entry<${g.mapKeyType}, ${g.mapValueType}> entry : this.${g.internalRefFieldName}){
        <#if g.mapKeyTypeIsGenerated>
        ${g.mapKeyTypeSourceFullName} key = entry.getKey().toSchema();
        <#else>
        ${g.mapKeyTypeSourceFullName} key = entry.getKey();
        </#if>
        <#if g.mapValueTypeIsGenerated>
        ${g.mapValueTypeSourceFullName} value = entry.getValue().toSchema();
        <#else>
        ${g.mapValueTypeSourceFullName} value = entry.getValue();
        </#if>
        ${g.fieldName}.put(key, value);
    }
    valToInit.${g.setterName}(${g.fieldName});
    }
    </#if>
<#elseif g.genericTypeIsGenerated>
    valToInit.${g.setterName}(this.${g.internalRefFieldName}.toSchema());

<#else>
    valToInit.${g.setterName}(this.${g.internalRefFieldName});
</#if>
</#list>
  }

  public ${sourceClass} toSchema(){
    ${sourceClass} retVal = new ${sourceClass}();
<#if super??>
    super.initSchema(retVal);
</#if>
    initSchema(retVal);
    return retVal;
  }
}
