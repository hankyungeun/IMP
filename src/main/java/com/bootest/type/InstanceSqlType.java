package com.bootest.type;

public enum InstanceSqlType {
    LINUX_SQL("linux-sql"),
    LINUX_SQL_WEB("linux-sql-web"),
    LINUX_SQL_ENT("linux-sql-ent"),
    RHEL_SQL("rhel-sql"),
    RHEL_SQL_WEB("rhel-sql-web"),
    RHEL_SQL_ENT("rhel-sql-ent"),
    WINDOWS_SQL("windows-sql"),
    WINDOWS_SQL_WEB("windows-sql-web"),
    WINDOWS_SQL_ENT("windows-sql-ent"),
    WINDOWS("windows"),
    SUSE("suse"),
    RHEL("rhel"),
    UBUNTU("ubuntu"),
    UBUNTU_SQL("ubuntu-sql"),
    UBUNTU_SQL_WEB("ubuntu-sql-web"),
    UBUNTU_SQL_ENT("ubuntu-sql-ent"),
    LINUX("linux");

    private String value;

    private InstanceSqlType(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
