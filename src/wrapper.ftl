package ${package};

<#list imports as i>
import ${i};
</#list>

/**
this is a wrapper classes generated from ${sourceClass}
*/

public class ${className} <#if super??>extends ${super}</#if>{
<#list privateWrapFields as pf>
    private final ${pf.type} ${pf.name};
    /** generate internal collection fields for sake of performance */
    <#list getters as g>
    <#if g.isCollection>
    private final ${g.retType} ${g.internalRefFieldName};
    </#if>
    </#list>
</#list>

    public ${className} (${sourceClass} val){
        <#if super??>
        super(val);
        </#if>
        this.refVal = val;

        <#list getters as g>
        <#if g.isCollection>
        this.${g.internalRefFieldName} = new ${g.collectionImplType}<>();
        <#if g.collectionImplType == 'ArrayList' || g.collectionImplType == 'HashSet'>
        for(${g.sourceClass} v : refVal.${g.retMethod}()){
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
        </#if>
        <#if g.isCollection>
        this.${g.internalRefFieldName} = new ${g.collectionImplType}<>();
        <#if g.collectionImplType == 'ArrayList' || g.collectionImplType == 'HashSet'>
        for(${g.sourceClass} v : refVal.${g.retMethod}()){
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
        </#if>
        </#list>
    }

<#list getters as g>
    public ${g.retType} ${g.methodName}(){
     <#if g.isCollection>
     return this.${g.internalRefFieldName};
     <#elseif g.fundamentalType>
        return refVal.${g.retMethod}();
     <#else>
        return new ${g.retType}(refVal.${g.retMethod}());
      </#if>
    }
</#list>
}
