package com.example.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.api.GeminiApi;
import com.example.data.AutomationRule;
import com.example.data.AutomationRuleStore;
import com.example.service.WhatsAppNotificationListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public final class AutomationRulesActivity extends Activity {
    private static final int PICK_SOURCE_CONTACT = 1;
    private static final int PICK_RECIPIENT_CONTACT = 2;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<AutomationRule> rules = new ArrayList<>();
    private LinearLayout root;
    private EditText instructionInput;
    private TextView statusView;
    private TextView sourceContactView;
    private TextView recipientContactView;
    private TextView previewView;
    private LinearLayout rulesContainer;
    private Button confirmButton;
    private String sourceName = "";
    private String sourcePhone = "";
    private String recipientName = "";
    private String recipientPhone = "";
    private String action = "";
    private String replyInstruction = "";
    private String fixedReply = "";
    private String summary = "";
    private String sourceContactQuery = "";
    private String recipientContactQuery = "";
    private String editingRuleId = "";
    private boolean hasDraft;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildView();
        loadRules();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    private void buildView() {
        ScrollView scrollView = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        root.setPadding(padding, padding, padding, padding);
        scrollView.addView(root);
        setContentView(scrollView);

        addText("AI Automation Rules", 24);
        addText("Write a Bengali or English instruction. Gemini only drafts a rule; nothing runs until you review and confirm it. Active AI-reply rules send matching incoming messages to Gemini and reply automatically.", 16);

        instructionInput = new EditText(this);
        instructionInput.setHint("উদাহরণ: রহিম মেসেজ দিলে বাংলায় সংক্ষিপ্ত উত্তর দেবে");
        instructionInput.setMinLines(3);
        instructionInput.setGravity(48);
        root.addView(instructionInput, matchWrap());
        addButton("Interpret instruction", this::interpretInstruction);

        sourceContactView = addText("Sender contact: not selected", 16);
        addButton("Choose sender contact", () -> pickContact(PICK_SOURCE_CONTACT));
        recipientContactView = addText("Recipient contact: not selected", 16);
        addButton("Choose forwarding recipient", () -> pickContact(PICK_RECIPIENT_CONTACT));
        addText("Sender matching uses the exact displayed notification name, not the selected phone number. Rules skip group notifications; WhatsApp notifications do not reliably expose a sender phone number.", 14);

        previewView = addText("Rule preview will appear here.", 16);
        statusView = addText("", 14);
        confirmButton = addButton("Review and activate", this::confirmDraft);
        confirmButton.setEnabled(false);

        addText("Saved rules", 20);
        rulesContainer = new LinearLayout(this);
        rulesContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(rulesContainer, matchWrap());
    }

    private void interpretInstruction() {
        String instruction = instructionInput.getText().toString().trim();
        if (instruction.isEmpty()) {
            statusView.setText("Enter an instruction first.");
            return;
        }
        editingRuleId = "";
        hasDraft = false;
        confirmButton.setEnabled(false);
        sourceName = "";
        sourcePhone = "";
        recipientName = "";
        recipientPhone = "";
        sourceContactQuery = "";
        recipientContactQuery = "";
        sourceContactView.setText("Sender contact: not selected");
        recipientContactView.setText("Recipient contact: not selected");
        statusView.setText("Interpreting instruction with Gemini…");
        String selectedModel = getSharedPreferences(WhatsAppNotificationListener.PREFS_NAME, MODE_PRIVATE)
                .getString(WhatsAppViewModel.KEY_CHATBOT_MODEL, "gemini-3.1-flash-lite-preview");
        executor.execute(() -> {
            try {
                JSONObject result = GeminiApi.INSTANCE.interpretAutomationInstruction(instruction, selectedModel);
                String parsedAction = result.getString("action");
                if (!AutomationRule.ACTION_AI_REPLY.equals(parsedAction)
                        && !AutomationRule.ACTION_FIXED_REPLY.equals(parsedAction)
                        && !AutomationRule.ACTION_FORWARD_REVIEW.equals(parsedAction)) {
                    throw new IllegalArgumentException(result.optString("summary", "This instruction is not supported."));
                }
                String sourceQuery = result.optString("source_contact_query").trim();
                if (sourceQuery.isEmpty()) {
                    throw new IllegalArgumentException("AI could not identify the sender. Make the sender explicit in your instruction.");
                }
                String recipientQuery = result.optString("recipient_contact_query").trim();
                String parsedReplyInstruction = result.optString("reply_instruction").trim();
                String parsedFixedReply = result.optString("fixed_reply").trim();
                if (AutomationRule.ACTION_AI_REPLY.equals(parsedAction) && parsedReplyInstruction.isEmpty()) {
                    parsedReplyInstruction = "Reply helpfully and naturally.";
                }
                if (AutomationRule.ACTION_FIXED_REPLY.equals(parsedAction) && parsedFixedReply.isEmpty()) {
                    throw new IllegalArgumentException("AI did not identify the exact reply text.");
                }
                if (AutomationRule.ACTION_FORWARD_REVIEW.equals(parsedAction) && recipientQuery.isEmpty()) {
                    throw new IllegalArgumentException("AI could not identify the recipient. Make the recipient explicit in your instruction.");
                }
                String finalReplyInstruction = parsedReplyInstruction;
                String finalReplyText = parsedFixedReply;
                runOnUiThread(() -> {
                    action = parsedAction;
                    replyInstruction = finalReplyInstruction;
                    fixedReply = finalReplyText;
                    sourceContactQuery = sourceQuery;
                    recipientContactQuery = recipientQuery;
                    summary = result.optString("summary", "Automation rule drafted.");
                    hasDraft = true;
                    updatePreview();
                    statusView.setText("Draft ready. Choose the sender contact, and choose a recipient for forwarding rules.");
                });
            } catch (IOException | JSONException | IllegalArgumentException exception) {
                runOnUiThread(() -> statusView.setText("Could not create a rule draft: " + exception.getMessage()));
            }
        });
    }

    private void pickContact(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode != PICK_SOURCE_CONTACT && requestCode != PICK_RECIPIENT_CONTACT)
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }
        Uri contactUri = data.getData();
        String[] columns = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        try (Cursor cursor = getContentResolver().query(contactUri, columns, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                statusView.setText("The selected contact could not be read.");
                return;
            }
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
                statusView.setText("Choose a contact entry that has both a name and a phone number.");
                return;
            }
            if (requestCode == PICK_SOURCE_CONTACT) {
                sourceName = name.trim();
                sourcePhone = phone.trim();
                sourceContactView.setText("Sender contact: " + sourceName + " (" + sourcePhone + ")");
            } else {
                recipientName = name.trim();
                recipientPhone = phone.trim();
                recipientContactView.setText("Recipient contact: " + recipientName + " (" + recipientPhone + ")");
            }
            updatePreview();
        } catch (SecurityException exception) {
            statusView.setText("Android did not grant access to the selected contact: " + exception.getMessage());
        }
    }

    private void updatePreview() {
        if (!hasDraft) {
            confirmButton.setEnabled(false);
            return;
        }
        StringBuilder preview = new StringBuilder();
        preview.append("AI summary: ").append(summary)
                .append("\nInstruction: ").append(instructionInput.getText().toString().trim())
                .append("\nAI identified sender as: ").append(sourceContactQuery)
                .append("\nSender match: ").append(sourceName.isEmpty() ? "choose a contact" : sourceName)
                .append("\nAction: ").append(actionLabel());
        if (AutomationRule.ACTION_AI_REPLY.equals(action)) {
            preview.append("\nReply instruction: ").append(replyInstruction)
                    .append("\nPrivacy and behavior: the incoming message is sent to Gemini; its reply is sent automatically using WhatsApp's notification reply action.");
        } else if (AutomationRule.ACTION_FIXED_REPLY.equals(action)) {
            preview.append("\nExact reply: ").append(fixedReply);
        } else if (AutomationRule.ACTION_FORWARD_REVIEW.equals(action)) {
            preview.append("\nRecipient: ").append(recipientName.isEmpty() ? "choose a contact" : recipientName)
                    .append("\nAI identified recipient as: ").append(recipientContactQuery)
                    .append("\nSafety: a notification will open a prefilled WhatsApp chat; you must tap Send yourself.");
        }
        preview.append("\n\nAfter activation, this rule persists until you edit, disable, or delete it. While any rule is enabled, unmatched chats will not receive the general auto-reply.");
        previewView.setText(preview.toString());
        confirmButton.setEnabled(!sourceName.isEmpty()
                && (!AutomationRule.ACTION_FORWARD_REVIEW.equals(action)
                || (!recipientName.isEmpty() && isInternationalPhone(recipientPhone))));
    }

    private boolean isInternationalPhone(String phone) {
        if (phone == null || !phone.trim().startsWith("+")) {
            return false;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 8 && digits.length() <= 15;
    }

    private String actionLabel() {
        if (AutomationRule.ACTION_AI_REPLY.equals(action)) {
            return "AI reply in the incoming chat";
        }
        if (AutomationRule.ACTION_FIXED_REPLY.equals(action)) {
            return "Fixed reply in the incoming chat";
        }
        return "Manual send review to another contact";
    }

    private void confirmDraft() {
        if (!hasDraft || sourceName.isEmpty()) {
            return;
        }
        if (AutomationRule.ACTION_FORWARD_REVIEW.equals(action) && !isInternationalPhone(recipientPhone)) {
            statusView.setText("Choose a recipient number saved in international +countrycode format for WhatsApp handoff.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Activate automation rule?")
                .setMessage(previewView.getText())
                .setNegativeButton("Review again", null)
                .setPositiveButton("Confirm and activate", (dialog, which) -> saveDraft())
                .show();
    }

    private void saveDraft() {
        try {
            AutomationRule rule = new AutomationRule(
                    editingRuleId.isEmpty() ? java.util.UUID.randomUUID().toString() : editingRuleId,
                    instructionInput.getText().toString().trim(),
                    sourceName,
                    sourcePhone,
                    action,
                    replyInstruction,
                    fixedReply,
                    recipientName,
                    recipientPhone,
                    true);
            for (AutomationRule existing : rules) {
                if (!existing.getId().equals(editingRuleId)
                        && existing.getSourceContactName().equalsIgnoreCase(sourceName)) {
                    statusView.setText("A rule already exists for this sender. Edit or delete it before adding another.");
                    return;
                }
            }
            if (editingRuleId.isEmpty()) {
                rules.add(rule);
            } else {
                boolean replaced = false;
                for (int index = 0; index < rules.size(); index++) {
                    if (rules.get(index).getId().equals(editingRuleId)) {
                        rules.set(index, rule);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    statusView.setText("This rule was removed before your changes could be saved. Reload the rules and try again.");
                    loadRules();
                    return;
                }
            }
            AutomationRuleStore.saveRules(this, rules);
            editingRuleId = "";
            statusView.setText("Rule saved and active.");
            renderRules();
            clearDraft();
        } catch (JSONException | IllegalArgumentException exception) {
            statusView.setText("Could not save the rule: " + exception.getMessage());
        } catch (IllegalStateException exception) {
            statusView.setText("Could not save the rule: " + exception.getMessage());
        }
    }

    private void clearDraft() {
        hasDraft = false;
        confirmButton.setEnabled(false);
        previewView.setText("Rule preview will appear here.");
        instructionInput.setText("");
        sourceName = "";
        sourcePhone = "";
        recipientName = "";
        recipientPhone = "";
        sourceContactView.setText("Sender contact: not selected");
        recipientContactView.setText("Recipient contact: not selected");
    }

    private void loadRules() {
        try {
            rules.clear();
            rules.addAll(AutomationRuleStore.getRules(this));
            renderRules();
        } catch (JSONException exception) {
            statusView.setText("Saved rules could not be read: " + exception.getMessage());
        }
    }

    private void renderRules() {
        rulesContainer.removeAllViews();
        if (rules.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No rules saved yet.");
            empty.setTextSize(14);
            rulesContainer.addView(empty, matchWrap());
            return;
        }
        for (AutomationRule rule : new ArrayList<>(rules)) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(10), dp(10), dp(10), dp(10));
            TextView details = new TextView(this);
            details.setText(rule.getSourceContactName() + " — " + labelFor(rule.getAction()) + "\n" + rule.getInstruction());
            row.addView(details, matchWrap());
            Switch enabledSwitch = new Switch(this);
            enabledSwitch.setText("Enabled");
            enabledSwitch.setChecked(rule.isEnabled());
            enabledSwitch.setOnCheckedChangeListener((button, checked) -> updateRule(rule.withEnabled(checked)));
            row.addView(enabledSwitch, matchWrap());
            LinearLayout buttons = new LinearLayout(this);
            buttons.addView(createButton("Edit", view -> editRule(rule)), weightedWrap());
            buttons.addView(createButton("Delete", view -> confirmDelete(rule)), weightedWrap());
            row.addView(buttons, matchWrap());
            rulesContainer.addView(row, matchWrap());
        }
    }

    private String labelFor(String ruleAction) {
        if (AutomationRule.ACTION_AI_REPLY.equals(ruleAction)) {
            return "AI reply";
        }
        if (AutomationRule.ACTION_FIXED_REPLY.equals(ruleAction)) {
            return "Fixed reply";
        }
        return "Manual send review";
    }

    private void updateRule(AutomationRule updatedRule) {
        replaceRule(updatedRule);
        persistAndRender();
    }

    private void editRule(AutomationRule rule) {
        editingRuleId = rule.getId();
        instructionInput.setText(rule.getInstruction());
        sourceName = rule.getSourceContactName();
        sourcePhone = rule.getSourcePhone();
        recipientName = rule.getRecipientName();
        recipientPhone = rule.getRecipientPhone();
        sourceContactQuery = sourceName;
        recipientContactQuery = recipientName;
        action = rule.getAction();
        replyInstruction = rule.getReplyInstruction();
        fixedReply = rule.getFixedReply();
        summary = "Review your existing rule before saving changes.";
        hasDraft = true;
        sourceContactView.setText("Sender contact: " + sourceName + " (" + sourcePhone + ")");
        recipientContactView.setText(recipientName.isEmpty()
                ? "Recipient contact: not selected"
                : "Recipient contact: " + recipientName + " (" + recipientPhone + ")");
        updatePreview();
        statusView.setText("Edit the instruction or contact mapping, then review and confirm.");
    }

    private void confirmDelete(AutomationRule rule) {
        new AlertDialog.Builder(this)
                .setTitle("Delete rule?")
                .setMessage("Delete the rule for " + rule.getSourceContactName() + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    rules.removeIf(item -> item.getId().equals(rule.getId()));
                    persistAndRender();
                })
                .show();
    }

    private void replaceRule(AutomationRule updatedRule) {
        for (int index = 0; index < rules.size(); index++) {
            if (rules.get(index).getId().equals(updatedRule.getId())) {
                rules.set(index, updatedRule);
                return;
            }
        }
    }

    private void persistAndRender() {
        try {
            AutomationRuleStore.saveRules(this, rules);
            renderRules();
        } catch (JSONException | IllegalStateException exception) {
            statusView.setText("Could not update saved rules: " + exception.getMessage());
        }
    }

    private TextView addText(String text, int sizeSp) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setPadding(0, dp(8), 0, dp(8));
        root.addView(view, matchWrap());
        return view;
    }

    private Button addButton(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(view -> action.run());
        root.addView(button, matchWrap());
        return button;
    }

    private Button createButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams weightedWrap() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
