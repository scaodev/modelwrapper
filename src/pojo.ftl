package ${package};

<#list imports as i>
import ${i};
</#list>

/**
this is a wrapper classes generated from ${sourceClass}
*/

public class ${className} <#if super??>extends ${super}</#if>{
<#list privateWrapFields as pf>
    private ${pf.type} ${pf.name};
</#list>

public ${className} (${sourceClass} val){
<#if super??>
    super(val);
</#if>
    this.refVal = val;
}

public ${className} (){
    <#if super??>
    this.refVal = new ${className}();
    </#if>
}

<#list getters as g>
public ${g.retType} ${g.methodName}(){
    <#if g.isCollection>
    <#if g.collectionImplType == 'ArrayList'>
    List<${g.genericTypes[0]}> retVal = new ArrayList<>();
    for(${g.sourceClass} v : refVal.${g.retMethod}()){
        <#if g.genericTypeIsGenerated>
        retVal.add(new ${g.genericTypes[0]}(v));
        <#else>
        retVal.add(v);
        </#if>
    }
    return retVal;
    <#elseif g.collectionImplType == 'HashSet'>
    Set<${g.genericTypes[0]}> retVal = new HashSet<>();
    for(${g.sourceClass} v : refVal.${g.retMethod}()){
        <#if g.genericTypeIsGenerated>
        retVal.add(new ${g.genericTypes[0]}(v));
        <#else>
        retVal.add(v);
        </#if>
    }

        <#else>
    Map<${g.mapKeyType}, ${g.mapKeyType}> retVal = new HashSet<>();
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
        retVal.put(key, value);
        }
    return retVal;
        </#if>
    <#elseif g.fundamentalType>
    return this.refVal.${g.retMethod}();
    <#else>
    return new ${g.retType}(refVal.${g.retMethod}());
    </#if>

}

public void ${g.setterName}(${g.retType} val){
    <#if g.isCollection>
        <#if g.collectionImplType == 'ArrayList'>
        this.refVal.${g.retMethod}().clear();
        for(${g.sourceClass} s : val){
            this.refVal.${g.retMethod}().add(s.toSchema());
        }
        <#else>
        this.refVal.${g.retMethod}().clear();
        for(Map.Entry<${g.mapKeyType}, ${g.mapValueType}> entry : val.entrySet()){
            <#if g.mapKeyTypeIsGenerated && g.mapValueTypeIsGenerated>
            this.refVal.${g.retMethod}().put(entry.key.toSchema(), entry.value.toSchema());
            <#elseif g.mapKeyTypeIsGenerated>
            this.refVal.${g.retMethod}().put(entry.key.toSchema(), entry.value);
            <#elseif g.mapValueTypeIsGenerated>
            this.refVal.${g.retMethod}().put(entry.key, entry.value.toSchema());
            <#else>
            this.refVal.${g.retMethod}().put(entry.key, entry.value);
            </#if>
        }
        </#if>
    <#elseif g.fundamentalType>
    this.refVal.${g.setterName}(val);
    <#else>
    this.refVal.${g.setterName}(val.toSchema());
    </#if>
}
</#list>
}
