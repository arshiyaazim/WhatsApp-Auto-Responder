package com.example.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.service.WhatsAppNotificationListener;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class AutomationRuleStore {
    private static final String TAG = "AutomationRuleStore";
    private static final String KEY_RULES = "automation_rules";

    private AutomationRuleStore() {
    }

    public static List<AutomationRule> getRules(Context context) throws JSONException {
        SharedPreferences preferences = getPreferences(context);
        String storedRules = preferences.getString(KEY_RULES, "[]");
        JSONArray array = new JSONArray(storedRules);
        List<AutomationRule> rules = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            JSONObject item = array.getJSONObject(index);
            try {
                rules.add(new AutomationRule(
                        item.getString("id"),
                        item.getString("instruction"),
                        item.getString("sourceContactName"),
                        item.getString("sourcePhone"),
                        item.getString("action"),
                        item.getString("replyInstruction"),
                        item.getString("fixedReply"),
                        item.getString("recipientName"),
                        item.getString("recipientPhone"),
                        item.getBoolean("enabled")));
            } catch (IllegalArgumentException exception) {
                Log.e(TAG, "Invalid stored automation rule at index " + index, exception);
                throw new JSONException("Invalid stored automation rule at index " + index);
            }
        }
        return rules;
    }

    public static void saveRules(Context context, List<AutomationRule> rules) throws JSONException {
        JSONArray array = new JSONArray();
        for (AutomationRule rule : rules) {
            JSONObject item = new JSONObject();
            item.put("id", rule.getId());
            item.put("instruction", rule.getInstruction());
            item.put("sourceContactName", rule.getSourceContactName());
            item.put("sourcePhone", rule.getSourcePhone());
            item.put("action", rule.getAction());
            item.put("replyInstruction", rule.getReplyInstruction());
            item.put("fixedReply", rule.getFixedReply());
            item.put("recipientName", rule.getRecipientName());
            item.put("recipientPhone", rule.getRecipientPhone());
            item.put("enabled", rule.isEnabled());
            array.put(item);
        }
        boolean saved = getPreferences(context).edit().putString(KEY_RULES, array.toString()).commit();
        if (!saved) {
            throw new IllegalStateException("Could not persist automation rules");
        }
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(WhatsAppNotificationListener.PREFS_NAME, Context.MODE_PRIVATE);
    }
}
