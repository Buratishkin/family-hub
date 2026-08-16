package org.buratishkin.familyhub.shared.outbox;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.lang.reflect.Method;

final class OutboxEventMetadata {
    private static final String[] AGGREGATE_ID_ACCESSORS = {
            "taskId",
            "familyId",
            "addressId",
            "categoryId",
            "inviteId",
            "aliasId",
            "memberId",
            "userId"
    };

    private OutboxEventMetadata() {
    }

    static String aggregateType(DomainEvent event) {
        String name = event.getClass().getSimpleName();
        if (name.endsWith("Event")) {
            name = name.substring(0, name.length() - "Event".length());
        }

        int splitAt = firstActionIndex(name);
        if (splitAt > 0) {
            return name.substring(0, splitAt);
        }
        return name;
    }

    static String aggregateId(DomainEvent event) {
        Object primaryValue = readAccessor(event, decapitalize(aggregateType(event)) + "Id");
        if (primaryValue != null) {
            return primaryValue.toString();
        }

        for (String accessor : AGGREGATE_ID_ACCESSORS) {
            Object value = readAccessor(event, accessor);
            if (value != null) {
                return value.toString();
            }
        }
        return "unknown";
    }

    private static int firstActionIndex(String name) {
        String[] actions = {
                "AdminChanged",
                "Created",
                "Updated",
                "Deleted",
                "Archived",
                "Added",
                "Removed",
                "Assigned",
                "Changed",
                "Redeemed",
                "Registered"
        };
        int first = -1;
        for (String action : actions) {
            int index = name.indexOf(action);
            if (index > 0 && (first == -1 || index < first)) {
                first = index;
            }
        }
        return first;
    }

    private static Object readAccessor(DomainEvent event, String accessor) {
        try {
            Method method = event.getClass().getMethod(accessor);
            return method.invoke(event);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String decapitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }
}
