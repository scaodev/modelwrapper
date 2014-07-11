package ${targetPackage};

<#list imports as i>
import ${i};
</#list>

${modifier}<#if !concrete> abstract</#if> class ${targetClassName} <#if !enumm && superClass??>extends ${superClass}</#if>{
}
