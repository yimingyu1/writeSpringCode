package org.demospirng;

/**
 * @author yimingyu
 */
public enum ScopeEnum {

    SINGLETON("singleton", "εδΎ"),
    PROTOTYPE("prototype", "εε");

    private String type;
    private String desc;

    private ScopeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
