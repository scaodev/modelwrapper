package ${targetPackage};

public enum ${targetClassName}{
<#list fields as f>
  ${f}<#if (f_has_next)>,<#else>;</#if>
</#list>

    public String value() {
        return name();
    }

    public static ${targetClassName} fromValue(String v) {
        return valueOf(v);
    }

    public static BrandEnum fromSchema(${sourceEnum} schema) {
        return fromValue(schema.value());
    }

    public ${sourceEnum} toSchema() {
        return ${sourceEnum}.fromValue(value());
    }

}
