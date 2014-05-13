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
    </#list>

    public ${className} (${sourceClass} val){
        <#if super??>
        super(val);
        </#if>
        this.refVal = val;
    }

    <#list getters as g>
    public ${g.retType} ${g.methodName}(){
        <#if "list" == g.wrapperType>
        ${g.retType} retVal = new ArrayList<${g.targetClass}>();
        for(${g.sourceClass} v : refVal.${g.retMethod}()){
            retVal.add(new ${g.genericTypes[0]}(v));
        }
        return retVal;
        <#elseif g.fundamentalType>
        return refVal.${g.retMethod}();
        <#else>
        return new ${g.retType}(refVal.${g.retMethod}());
        </#if>
    }
    </#list>
}
