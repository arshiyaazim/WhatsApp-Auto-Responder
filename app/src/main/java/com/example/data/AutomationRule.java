package com.example.data;

import java.util.Objects;
import java.util.UUID;

public final class AutomationRule {
    public static final String ACTION_AI_REPLY = "AI_REPLY";
    public static final String ACTION_FIXED_REPLY = "FIXED_REPLY";
    public static final String ACTION_FORWARD_REVIEW = "FORWARD_REVIEW";

    private final String id;
    private final String instruction;
    private final String sourceContactName;
    private final String sourcePhone;
    private final String action;
    private final String replyInstruction;
    private final String fixedReply;
    private final String recipientName;
    private final String recipientPhone;
    private final boolean enabled;

    public AutomationRule(
            String id,
            String instruction,
            String sourceContactName,
            String sourcePhone,
            String action,
            String replyInstruction,
            String fixedReply,
            String recipientName,
            String recipientPhone,
            boolean enabled) {
        this.id = requireText(id, "id");
        this.instruction = requireText(instruction, "instruction");
        this.sourceContactName = requireText(sourceContactName, "sourceContactName");
        this.sourcePhone = requireText(sourcePhone, "sourcePhone");
        this.action = requireAction(action);
        this.replyInstruction = Objects.requireNonNull(replyInstruction, "replyInstruction");
        this.fixedReply = Objects.requireNonNull(fixedReply, "fixedReply");
        this.recipientName = Objects.requireNonNull(recipientName, "recipientName");
        this.recipientPhone = Objects.requireNonNull(recipientPhone, "recipientPhone");
        this.enabled = enabled;
    }

    public static AutomationRule create(
            String instruction,
            String sourceContactName,
            String sourcePhone,
            String action,
            String replyInstruction,
            String fixedReply,
            String recipientName,
            String recipientPhone) {
        return new AutomationRule(
                UUID.randomUUID().toString(),
                instruction,
                sourceContactName,
                sourcePhone,
                action,
                replyInstruction,
                fixedReply,
                recipientName,
                recipientPhone,
                true);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String requireAction(String action) {
        requireText(action, "action");
        if (!ACTION_AI_REPLY.equals(action)
                && !ACTION_FIXED_REPLY.equals(action)
                && !ACTION_FORWARD_REVIEW.equals(action)) {
            throw new IllegalArgumentException("Unsupported automation action: " + action);
        }
        return action;
    }

    public AutomationRule withEnabled(boolean enabled) {
        return new AutomationRule(
                id,
                instruction,
                sourceContactName,
                sourcePhone,
                action,
                replyInstruction,
                fixedReply,
                recipientName,
                recipientPhone,
                enabled);
    }

    public String getId() {
        return id;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getSourceContactName() {
        return sourceContactName;
    }

    public String getSourcePhone() {
        return sourcePhone;
    }

    public String getAction() {
        return action;
    }

    public String getReplyInstruction() {
        return replyInstruction;
    }

    public String getFixedReply() {
        return fixedReply;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
