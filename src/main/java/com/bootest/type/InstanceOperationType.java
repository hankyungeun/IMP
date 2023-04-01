package com.bootest.type;

public enum InstanceOperationType {
    LINUX("RunInstances"),
    LINUX_SQL("RunInstances:0004"),
    LINUX_SQL_WEB("RunInstances:0200"),
    LINUX_SQL_ENT("RunInstances:0100"),
    RHEL_BYOL("RunInstances:00g0"),
    RHEL("RunInstances:0010"),
    RHEL_HA("RunInstances:1010"),
    RHEL_HA_SQL("RunInstances:1014"),
    RHEL_HA_ENT_SQL("RunInstances:1110"),
    RHEL_SQL("RunInstances:0014"),
    RHEL_SQL_WEB("RunInstances:0210"),
    RHEL_SQL_ENT("RunInstances:0110"),
    WINDOWS_SQL("RunInstances:0006"),
    WINDOWS_SQL_WEB("RunInstances:0202"),
    WINDOWS_SQL_ENT("RunInstances:0102"),
    WINDOWS("RunInstances:0002"),
    WINDOWS_BYOL("RunInstances:0800"),
    SUSE("RunInstances:000g");

    private String value;

    private InstanceOperationType(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
