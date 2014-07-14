package ${targetPackage};

<#list imports as i>
import ${i};
</#list>

${modifier}<#if !concrete> abstract</#if> class ${targetClassName} <#if !enumm && superClass??>extends ${superClass}</#if>{
<#list getters as g>
    ${g.modifier} ${g.returnClass} ${g.fieldName};
</#list>

<#list getters as g>

    public ${g.returnClass} ${g.getterName}(){
        return ${g.fieldName};
    };
    public void ${g.setterName}(${g.returnClass} val){
        this.${g.fieldName} = val;
    }
</#list>

<#list inners as inner>
    public static class ${inner.targetClassName}{
        <#list inner.getters as g>
        ${g.modifier} ${g.returnClass} ${g.fieldName};
        </#list>

        <#list inner.getters as g>

        public ${g.returnClass} ${g.getterName}(){
        return ${g.fieldName};
        };
        public void ${g.setterName}(${g.returnClass} val){
        this.${g.fieldName} = val;
        }
        </#list>
    }

</#list>
}
