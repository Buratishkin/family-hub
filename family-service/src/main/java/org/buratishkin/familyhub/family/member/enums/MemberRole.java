package org.buratishkin.familyhub.family.member.enums;

public enum MemberRole {
    MEMBER,
    EVENT_EDITOR,
    ADMIN;

    public boolean canViewCalendar() {
        return true;
    }

    public boolean canAddEvents() {
        return this == EVENT_EDITOR || this == ADMIN;
    }

    public boolean canManageMembers() {
        return this == ADMIN;
    }
}
