package ${package};

<#list imports as i>
import ${i};
</#list>

/**
this is a wrapper classes generated from ${sourceClass}
*/

public class ${className}{
    <#list privateWrapFields as pf>
    private final ${pf.type} ${pf.name};
    </#list>

    private ${className} (${sourceClass} val){
        this.refVal = val;
    }

    <#list getters as g>
    public ${g.retType} ${g.methodName}(){
        return ${g.retInstance}.${g.retMethod}();
    }
    </#list>
}
